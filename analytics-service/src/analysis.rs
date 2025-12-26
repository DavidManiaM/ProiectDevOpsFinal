/// Calculate the moving average for the last N prices
pub fn calculate_moving_average(prices: &[f64], window: usize) -> Option<f64> {
    if prices.len() < window {
        return None;
    }

    let sum: f64 = prices.iter().rev().take(window).sum();
    Some(sum / window as f64)
}

/// Calculate the standard deviation of prices
pub fn calculate_std_dev(prices: &[f64]) -> Option<f64> {
    if prices.len() < 2 {
        return None;
    }

    let mean = prices.iter().sum::<f64>() / prices.len() as f64;
    let variance = prices.iter().map(|p| (p - mean).powi(2)).sum::<f64>() / prices.len() as f64;
    Some(variance.sqrt())
}

#[derive(Debug, Clone)]
pub struct AnomalyResult {
    pub anomaly_type: String,
    pub message: String,
}

/// Detect price anomalies
pub fn detect_anomaly(
    prices: &[f64],
    current_price: f64,
    percent_change: Option<f64>,
) -> Option<AnomalyResult> {
    // Check for significant price spike
    if let Some(pct) = percent_change {
        if pct > 5.0 {
            return Some(AnomalyResult {
                anomaly_type: "SPIKE_UP".to_string(),
                message: format!("Price spiked up {:.2}% in one interval", pct),
            });
        }
        if pct < -5.0 {
            return Some(AnomalyResult {
                anomaly_type: "SPIKE_DOWN".to_string(),
                message: format!("Price dropped {:.2}% in one interval", pct.abs()),
            });
        }
    }

    // Check for price deviation from moving average
    if prices.len() >= 20 {
        if let (Some(ma20), Some(std_dev)) = (
            calculate_moving_average(prices, 20),
            calculate_std_dev(prices),
        ) {
            let deviation = (current_price - ma20).abs();
            // Price is more than 2 standard deviations from MA20
            if deviation > 2.0 * std_dev {
                return Some(AnomalyResult {
                    anomaly_type: "ANOMALY".to_string(),
                    message: format!(
                        "Price deviated {:.2} from MA20 ({:.2}), exceeding 2 std devs ({:.2})",
                        deviation, ma20, std_dev
                    ),
                });
            }
        }
    }

    None
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_moving_average() {
        let prices = vec![10.0, 20.0, 30.0, 40.0, 50.0];
        assert_eq!(calculate_moving_average(&prices, 5), Some(30.0));
        assert_eq!(calculate_moving_average(&prices, 3), Some(40.0));
        assert_eq!(calculate_moving_average(&prices, 10), None);
    }

    #[test]
    fn test_spike_detection() {
        let prices = vec![100.0, 100.0, 100.0];

        // Test spike up
        let result = detect_anomaly(&prices, 110.0, Some(10.0));
        assert!(result.is_some());
        assert_eq!(result.unwrap().anomaly_type, "SPIKE_UP");

        // Test spike down
        let result = detect_anomaly(&prices, 90.0, Some(-10.0));
        assert!(result.is_some());
        assert_eq!(result.unwrap().anomaly_type, "SPIKE_DOWN");

        // Test normal change
        let result = detect_anomaly(&prices, 102.0, Some(2.0));
        assert!(result.is_none());
    }
}

