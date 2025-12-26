import { useState, useEffect } from 'react';
import { Price } from '../services/api';
import { TrendingUp, TrendingDown, Minus } from 'lucide-react';

interface PriceCardProps {
  price: Price;
  onClick: () => void;
  isSelected: boolean;
}

export default function PriceCard({ price, onClick, isSelected }: PriceCardProps) {
  const [flash, setFlash] = useState<'up' | 'down' | null>(null);
  const [prevPrice, setPrevPrice] = useState<number>(price.price);

  useEffect(() => {
    if (price.price !== prevPrice) {
      setFlash(price.price > prevPrice ? 'up' : 'down');
      setPrevPrice(price.price);
      const timer = setTimeout(() => setFlash(null), 500);
      return () => clearTimeout(timer);
    }
  }, [price.price, prevPrice]);

  const formatPrice = (value: number) => {
    if (value >= 1000) {
      return value.toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      });
    }
    return value.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 4,
    });
  };

  const formatChange = (value: number | null) => {
    if (value === null) return '--';
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
  };

  const getChangeColor = (value: number | null) => {
    if (value === null) return 'text-gray-500';
    return value >= 0 ? 'text-green-500' : 'text-red-500';
  };

  const getChangeIcon = (value: number | null) => {
    if (value === null) return <Minus className="h-4 w-4" />;
    return value >= 0 ? (
      <TrendingUp className="h-4 w-4" />
    ) : (
      <TrendingDown className="h-4 w-4" />
    );
  };

  const isNegative = (price.percentChange ?? 0) < 0;

  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-xl shadow-md p-4 cursor-pointer transition-all hover:shadow-lg ${
        isSelected ? 'ring-2 ring-blue-500' : ''
      } ${flash === 'up' ? 'flash-green' : flash === 'down' ? 'flash-red' : ''}`}
    >
      <div className="flex items-start justify-between">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-lg font-bold text-gray-900">
              {price.ticker}
            </span>
            <span
              className={`px-2 py-0.5 text-xs rounded-full ${
                price.ticker === 'BTC' || price.ticker === 'ETH' || price.ticker === 'SOL'
                  ? 'bg-orange-100 text-orange-700'
                  : 'bg-blue-100 text-blue-700'
              }`}
            >
              {price.ticker === 'BTC' || price.ticker === 'ETH' || price.ticker === 'SOL'
                ? 'Crypto'
                : 'Stock'}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">{price.symbolName}</p>
        </div>
        <div className={`flex items-center space-x-1 ${getChangeColor(price.percentChange)}`}>
          {getChangeIcon(price.percentChange)}
          <span className="text-sm font-medium">
            {formatChange(price.percentChange)}
          </span>
        </div>
      </div>

      <div className="mt-4">
        <div className="text-3xl font-bold text-gray-900">
          ${formatPrice(price.price)}
        </div>
      </div>

      {/* Moving Averages */}
      <div className="mt-4 grid grid-cols-2 gap-2 text-xs">
        <div className="bg-gray-50 rounded-lg p-2">
          <span className="text-gray-500">MA(5)</span>
          <span className="block font-medium text-gray-700">
            {price.movingAverage5
              ? `$${formatPrice(price.movingAverage5)}`
              : '--'}
          </span>
        </div>
        <div className="bg-gray-50 rounded-lg p-2">
          <span className="text-gray-500">MA(20)</span>
          <span className="block font-medium text-gray-700">
            {price.movingAverage20
              ? `$${formatPrice(price.movingAverage20)}`
              : '--'}
          </span>
        </div>
      </div>

      {/* Timestamp */}
      <div className="mt-3 text-xs text-gray-400 text-right">
        {new Date(price.timestamp).toLocaleTimeString()}
      </div>
    </div>
  );
}

