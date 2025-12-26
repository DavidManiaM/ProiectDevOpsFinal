import { useEffect, useState } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { pricesApi, Price } from '../services/api';
import { RefreshCw } from 'lucide-react';

interface PriceChartProps {
  ticker: string;
}

export default function PriceChart({ ticker }: PriceChartProps) {
  const [data, setData] = useState<Price[]>([]);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState<'1h' | '6h' | '24h'>('1h');

  useEffect(() => {
    loadChartData();
  }, [ticker, timeRange]);

  const loadChartData = async () => {
    setLoading(true);
    try {
      const hours = timeRange === '1h' ? 1 : timeRange === '6h' ? 6 : 24;
      const response = await pricesApi.getVariations(ticker, hours);
      // Reverse to show chronological order
      setData(response.data.reverse());
    } catch (error) {
      console.error('Failed to load chart data:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatPrice = (value: number) => {
    return `$${value.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  };

  const chartData = data.map((p) => ({
    time: formatTime(p.timestamp),
    price: p.price,
    ma5: p.movingAverage5,
    ma20: p.movingAverage20,
  }));

  return (
    <div className="bg-white rounded-xl shadow-md p-4">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">
          {ticker} Price History
        </h3>
        <div className="flex items-center space-x-2">
          {/* Time Range Selector */}
          <div className="flex bg-gray-100 rounded-lg p-1">
            {(['1h', '6h', '24h'] as const).map((range) => (
              <button
                key={range}
                onClick={() => setTimeRange(range)}
                className={`px-3 py-1 text-sm rounded-md transition-colors ${
                  timeRange === range
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                {range}
              </button>
            ))}
          </div>
          <button
            onClick={loadChartData}
            className="p-2 text-gray-600 hover:text-blue-600 transition-colors"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {loading && data.length === 0 ? (
        <div className="h-64 flex items-center justify-center">
          <RefreshCw className="h-8 w-8 text-blue-500 animate-spin" />
        </div>
      ) : data.length === 0 ? (
        <div className="h-64 flex items-center justify-center text-gray-500">
          No price data available for this time range
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis
              dataKey="time"
              tick={{ fontSize: 12 }}
              tickLine={false}
              axisLine={{ stroke: '#e5e7eb' }}
            />
            <YAxis
              tickFormatter={formatPrice}
              tick={{ fontSize: 12 }}
              tickLine={false}
              axisLine={{ stroke: '#e5e7eb' }}
              domain={['auto', 'auto']}
            />
            <Tooltip
              formatter={(value: number) => [formatPrice(value), '']}
              labelStyle={{ color: '#374151' }}
              contentStyle={{
                backgroundColor: 'white',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
              }}
            />
            <Legend />
            <Line
              type="monotone"
              dataKey="price"
              stroke="#3b82f6"
              strokeWidth={2}
              dot={false}
              name="Price"
            />
            <Line
              type="monotone"
              dataKey="ma5"
              stroke="#22c55e"
              strokeWidth={1}
              strokeDasharray="5 5"
              dot={false}
              name="MA(5)"
            />
            <Line
              type="monotone"
              dataKey="ma20"
              stroke="#f59e0b"
              strokeWidth={1}
              strokeDasharray="5 5"
              dot={false}
              name="MA(20)"
            />
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

