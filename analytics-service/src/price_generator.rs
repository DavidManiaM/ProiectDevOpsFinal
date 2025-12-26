use rand::Rng;

/// Generates realistic price movements using random walk with mean reversion
pub struct PriceGenerator {
    current_price: f64,
    base_price: f64,
    volatility: f64,
    mean_reversion_speed: f64,
}

impl PriceGenerator {
    pub fn new(initial_price: f64, volatility: f64) -> Self {
        Self {
            current_price: initial_price,
            base_price: initial_price,
            volatility,
            mean_reversion_speed: 0.01,
        }
    }

    /// Generate the next price and volume
    pub fn next_price(&mut self) -> (f64, f64) {
        let mut rng = rand::thread_rng();

        // Random walk component - increased influence for more variation
        let random_change = rng.gen_range(-1.0..1.0) * self.volatility * 1.5;

        // Mean reversion component - very gentle to allow trends
        let deviation_pct = (self.current_price - self.base_price) / self.base_price;
        // Only apply mean reversion when price deviates more than 10%
        let mean_reversion = if deviation_pct.abs() > 0.1 {
            -deviation_pct * self.mean_reversion_speed * 0.5
        } else {
            0.0
        };

        // More frequent but varied news events for interesting movements
        let news_event = if rng.gen_ratio(1, 50) {
            rng.gen_range(-0.03..0.03) // 3% move on "news"
        } else if rng.gen_ratio(1, 10) {
            rng.gen_range(-0.01..0.01) // 1% micro-movements
        } else {
            0.0
        };

        // Calculate percentage change with wider bounds
        let total_change = (random_change + mean_reversion + news_event).clamp(-0.08, 0.08);
        self.current_price *= 1.0 + total_change;

        // Wider price bounds for more realistic market behavior (70% to 140% of base)
        let min_price = self.base_price * 0.7;
        let max_price = self.base_price * 1.4;
        self.current_price = self.current_price.clamp(min_price, max_price);

        // Generate volume with more variation
        let base_volume = rng.gen_range(50_000.0..2_000_000.0);
        let volume_multiplier = 1.0 + total_change.abs() * 8.0;
        let volume = base_volume * volume_multiplier;

        (self.current_price, volume)
    }

    /// Update volatility (useful for market conditions)
    pub fn set_volatility(&mut self, volatility: f64) {
        self.volatility = volatility;
    }

    /// Get current price
    pub fn current_price(&self) -> f64 {
        self.current_price
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_price_generation() {
        let mut generator = PriceGenerator::new(100.0, 0.01);

        for _ in 0..100 {
            let (price, volume) = generator.next_price();
            assert!(price > 0.0);
            assert!(volume > 0.0);
        }
    }

    #[test]
    fn test_price_stays_in_bounds() {
        let mut generator = PriceGenerator::new(100.0, 0.02);

        for _ in 0..1000 {
            let (price, _) = generator.next_price();
            // Price should stay between 70% and 140% of base (100.0)
            assert!(price >= 70.0 && price <= 140.0);
        }
    }
}

