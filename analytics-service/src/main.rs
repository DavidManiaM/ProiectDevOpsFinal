use axum::{
    extract::{Path, State},
    http::StatusCode,
    routing::{get, post},
    Json, Router,
};
use chrono::{DateTime, Utc};
use deadpool_postgres::{Config, Pool, Runtime};
use rand::Rng;
use serde::{Deserialize, Serialize};
use std::{collections::HashMap, env, net::SocketAddr, sync::Arc, time::Duration};
use tokio::sync::RwLock;
use tokio_postgres::NoTls;
use tower_http::cors::{Any, CorsLayer};
use tracing::{info, warn, error, Level};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

mod analysis;
mod price_generator;

use analysis::{calculate_moving_average, detect_anomaly, AnomalyResult};
use price_generator::PriceGenerator;

// Types
type SharedState = Arc<AppState>;

#[derive(Clone)]
struct AppState {
    db_pool: Pool,
    gateway_url: String,
    price_history: Arc<RwLock<HashMap<String, Vec<PricePoint>>>>,
    generators: Arc<RwLock<HashMap<String, PriceGenerator>>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
struct PricePoint {
    price: f64,
    volume: f64,
    timestamp: DateTime<Utc>,
}

#[derive(Debug, Serialize, Deserialize)]
struct AnalyticsPriceData {
    ticker: String,
    price: f64,
    volume: f64,
    moving_average_5: Option<f64>,
    moving_average_20: Option<f64>,
    percent_change: Option<f64>,
    timestamp: DateTime<Utc>,
    is_anomaly: bool,
    anomaly_type: Option<String>,
    anomaly_message: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
struct Symbol {
    id: i64,
    ticker: String,
    name: String,
    symbol_type: String,
}

#[derive(Debug, Serialize)]
struct HealthResponse {
    status: String,
    timestamp: DateTime<Utc>,
}

#[tokio::main]
async fn main() {
    // Load environment variables
    dotenv::dotenv().ok();

    // Initialize tracing
    tracing_subscriber::registry()
        .with(tracing_subscriber::EnvFilter::new(
            env::var("RUST_LOG").unwrap_or_else(|_| "info".into()),
        ))
        .with(tracing_subscriber::fmt::layer().json())
        .init();

    info!("Starting Analytics Service");

    // Database configuration
    let db_config = create_db_config();
    let pool = db_config
        .create_pool(Some(Runtime::Tokio1), NoTls)
        .expect("Failed to create database pool");

    // Gateway URL
    let gateway_url = env::var("GATEWAY_URL").unwrap_or_else(|_| "http://localhost:8080".to_string());

    // Create application state
    let state = Arc::new(AppState {
        db_pool: pool,
        gateway_url,
        price_history: Arc::new(RwLock::new(HashMap::new())),
        generators: Arc::new(RwLock::new(HashMap::new())),
    });

    // Initialize price generators for symbols
    initialize_generators(state.clone()).await;

    // Start price simulation task
    let simulation_state = state.clone();
    tokio::spawn(async move {
        run_price_simulation(simulation_state).await;
    });

    // Build router
    let app = Router::new()
        .route("/health", get(health_check))
        .route("/api/symbols", get(get_symbols))
        .route("/api/prices/:ticker", get(get_latest_price))
        .route("/api/simulate/start", post(start_simulation))
        .route("/api/simulate/stop", post(stop_simulation))
        .route("/metrics", get(metrics_handler))
        .layer(
            CorsLayer::new()
                .allow_origin(Any)
                .allow_methods(Any)
                .allow_headers(Any),
        )
        .with_state(state);

    // Start server
    let port: u16 = env::var("PORT")
        .unwrap_or_else(|_| "8081".to_string())
        .parse()
        .unwrap_or(8081);

    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    info!("Analytics service listening on {}", addr);

    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}

fn create_db_config() -> Config {
    let mut cfg = Config::new();
    cfg.host = Some(env::var("DB_HOST").unwrap_or_else(|_| "localhost".to_string()));
    cfg.port = Some(
        env::var("DB_PORT")
            .unwrap_or_else(|_| "5432".to_string())
            .parse()
            .unwrap_or(5432),
    );
    cfg.dbname = Some(env::var("DB_NAME").unwrap_or_else(|_| "stockmarket".to_string()));
    cfg.user = Some(env::var("DB_USER").unwrap_or_else(|_| "postgres".to_string()));

    // Check for password file (Docker Secret) first
    let password = if let Ok(path) = env::var("DB_PASSWORD_FILE") {
        match std::fs::read_to_string(path) {
            Ok(pwd) => pwd.trim().to_string(),
            Err(e) => {
                warn!("Failed to read DB password from file: {}", e);
                env::var("DB_PASSWORD").unwrap_or_else(|_| "postgres".to_string())
            }
        }
    } else {
        env::var("DB_PASSWORD").unwrap_or_else(|_| "postgres".to_string())
    };

    cfg.password = Some(password);
    cfg
}

async fn initialize_generators(state: SharedState) {
    let client = match state.db_pool.get().await {
        Ok(c) => c,
        Err(e) => {
            error!("Failed to get database connection: {}", e);
            return;
        }
    };

    let rows = match client
        .query("SELECT id, ticker, name, type FROM symbols", &[])
        .await
    {
        Ok(r) => r,
        Err(e) => {
            error!("Failed to fetch symbols: {}", e);
            return;
        }
    };

    let mut generators = state.generators.write().await;
    let mut history = state.price_history.write().await;

    for row in rows {
        let ticker: String = row.get("ticker");
        let symbol_type: String = row.get("type");

        // Set initial prices based on symbol type
        let initial_price = match symbol_type.as_str() {
            "CRYPTO" => match ticker.as_str() {
                "BTC" => 42000.0,
                "ETH" => 2200.0,
                "SOL" => 100.0,
                _ => 50.0,
            },
            "STOCK" => match ticker.as_str() {
                "AAPL" => 185.0,
                "GOOGL" => 140.0,
                "MSFT" => 375.0,
                "AMZN" => 155.0,
                "TSLA" => 250.0,
                _ => 100.0,
            },
            _ => 100.0,
        };

        let volatility = if symbol_type == "CRYPTO" { 0.02 } else { 0.005 };
        generators.insert(ticker.clone(), PriceGenerator::new(initial_price, volatility));
        history.insert(ticker.clone(), Vec::new());

        info!("Initialized generator for {}: ${:.2}", ticker, initial_price);
    }
}

async fn run_price_simulation(state: SharedState) {
    let client = reqwest::Client::new();
    let mut interval = tokio::time::interval(Duration::from_secs(5));

    loop {
        interval.tick().await;

        let mut generators = state.generators.write().await;
        let mut history = state.price_history.write().await;

        for (ticker, generator) in generators.iter_mut() {
            // Generate new price
            let (price, volume) = generator.next_price();
            let timestamp = Utc::now();

            let point = PricePoint {
                price,
                volume,
                timestamp,
            };

            // Update history
            let ticker_history = history.entry(ticker.clone()).or_insert_with(Vec::new);
            ticker_history.push(point.clone());

            // Keep only last 100 prices
            if ticker_history.len() > 100 {
                ticker_history.remove(0);
            }

            // Calculate analytics
            let prices: Vec<f64> = ticker_history.iter().map(|p| p.price).collect();
            let ma5 = calculate_moving_average(&prices, 5);
            let ma20 = calculate_moving_average(&prices, 20);

            let percent_change = if prices.len() > 1 {
                let prev = prices[prices.len() - 2];
                Some(((price - prev) / prev) * 100.0)
            } else {
                None
            };

            // Detect anomalies
            let anomaly = detect_anomaly(&prices, price, percent_change);

            let data = AnalyticsPriceData {
                ticker: ticker.clone(),
                price,
                volume,
                moving_average_5: ma5,
                moving_average_20: ma20,
                percent_change,
                timestamp,
                is_anomaly: anomaly.is_some(),
                anomaly_type: anomaly.as_ref().map(|a| a.anomaly_type.clone()),
                anomaly_message: anomaly.map(|a| a.message),
            };

            // Send to gateway
            let url = format!("{}/api/analytics/price", state.gateway_url);
            match client.post(&url).json(&data).send().await {
                Ok(response) => {
                    if response.status().is_success() {
                        info!("Sent price update for {}: ${:.2}", ticker, price);
                    } else {
                        warn!("Gateway returned error for {}: {}", ticker, response.status());
                    }
                }
                Err(e) => {
                    warn!("Failed to send price to gateway: {}", e);
                }
            }
        }
    }
}

// Handlers
async fn health_check() -> Json<HealthResponse> {
    Json(HealthResponse {
        status: "healthy".to_string(),
        timestamp: Utc::now(),
    })
}

async fn get_symbols(State(state): State<SharedState>) -> Result<Json<Vec<Symbol>>, StatusCode> {
    let client = state
        .db_pool
        .get()
        .await
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    let rows = client
        .query("SELECT id, ticker, name, type FROM symbols", &[])
        .await
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    let symbols: Vec<Symbol> = rows
        .iter()
        .map(|row| Symbol {
            id: row.get("id"),
            ticker: row.get("ticker"),
            name: row.get("name"),
            symbol_type: row.get("type"),
        })
        .collect();

    Ok(Json(symbols))
}

async fn get_latest_price(
    State(state): State<SharedState>,
    Path(ticker): Path<String>,
) -> Result<Json<Option<PricePoint>>, StatusCode> {
    let history = state.price_history.read().await;
    let price = history
        .get(&ticker.to_uppercase())
        .and_then(|h| h.last().cloned());

    Ok(Json(price))
}

async fn start_simulation() -> Json<&'static str> {
    info!("Simulation is always running");
    Json("Simulation is running")
}

async fn stop_simulation() -> Json<&'static str> {
    info!("Stop simulation requested (no-op)");
    Json("Simulation cannot be stopped in this mode")
}

async fn metrics_handler() -> String {
    // Basic metrics endpoint
    "# HELP analytics_service_up Analytics service is up\n\
     # TYPE analytics_service_up gauge\n\
     analytics_service_up 1\n"
        .to_string()
}

