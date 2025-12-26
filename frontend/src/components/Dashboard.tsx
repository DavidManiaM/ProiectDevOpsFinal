import { useEffect, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useWebSocket } from '../hooks/useWebSocket';
import { pricesApi, alertsApi, Price, Alert } from '../services/api';
import PriceCard from './PriceCard';
import AlertList from './AlertList';
import PriceChart from './PriceChart';
import {
  TrendingUp,
  Bell,
  LogOut,
  RefreshCw,
  Wifi,
  WifiOff,
} from 'lucide-react';

export default function Dashboard() {
  const { username, logout } = useAuth();
  const { connected, prices: wsPrices, alerts: wsAlerts } = useWebSocket();
  const [prices, setPrices] = useState<Price[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (wsPrices.length > 0) {
      setPrices((prev) => {
        const priceMap = new Map(prev.map((p) => [p.ticker, p]));
        wsPrices.forEach((p) => priceMap.set(p.ticker, p));
        return Array.from(priceMap.values());
      });
    }
  }, [wsPrices]);

  useEffect(() => {
    if (wsAlerts.length > 0) {
      setAlerts((prev) => {
        const newAlerts = [...wsAlerts, ...prev];
        return newAlerts.slice(0, 50);
      });
      setUnreadCount((prev) => prev + wsAlerts.filter((a) => !a.isRead).length);
    }
  }, [wsAlerts]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [pricesRes, alertsRes, unreadRes] = await Promise.all([
        pricesApi.getLatest(),
        alertsApi.getRecent(24),
        alertsApi.getUnreadCount(),
      ]);
      setPrices(pricesRes.data);
      setAlerts(alertsRes.data);
      setUnreadCount(unreadRes.data.count);
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await alertsApi.markAllAsRead();
      setAlerts((prev) => prev.map((a) => ({ ...a, isRead: true })));
      setUnreadCount(0);
    } catch (error) {
      console.error('Failed to mark alerts as read:', error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <TrendingUp className="h-8 w-8 text-blue-600" />
              <h1 className="text-2xl font-bold text-gray-900">
                Stock Market Dashboard
              </h1>
            </div>
            <div className="flex items-center space-x-4">
              {/* Connection Status */}
              <div
                className={`flex items-center space-x-2 px-3 py-1 rounded-full text-sm ${
                  connected
                    ? 'bg-green-100 text-green-700'
                    : 'bg-red-100 text-red-700'
                }`}
              >
                {connected ? (
                  <Wifi className="h-4 w-4" />
                ) : (
                  <WifiOff className="h-4 w-4" />
                )}
                <span>{connected ? 'Live' : 'Disconnected'}</span>
              </div>

              {/* Unread Alerts */}
              <div className="relative">
                <Bell className="h-6 w-6 text-gray-600" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </div>

              {/* Refresh */}
              <button
                onClick={loadData}
                className="p-2 text-gray-600 hover:text-blue-600 transition-colors"
                title="Refresh data"
              >
                <RefreshCw className={`h-5 w-5 ${loading ? 'animate-spin' : ''}`} />
              </button>

              {/* User Menu */}
              <div className="flex items-center space-x-3">
                <span className="text-sm text-gray-600">
                  Hello, <strong>{username}</strong>
                </span>
                <button
                  onClick={logout}
                  className="flex items-center space-x-1 text-gray-600 hover:text-red-600 transition-colors"
                >
                  <LogOut className="h-5 w-5" />
                  <span className="text-sm">Logout</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Price Cards */}
          <div className="lg:col-span-2">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              Live Prices
            </h2>
            {loading && prices.length === 0 ? (
              <div className="flex justify-center items-center h-64">
                <RefreshCw className="h-8 w-8 text-blue-500 animate-spin" />
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {prices.map((price) => (
                  <PriceCard
                    key={price.ticker}
                    price={price}
                    onClick={() => setSelectedSymbol(price.ticker)}
                    isSelected={selectedSymbol === price.ticker}
                  />
                ))}
              </div>
            )}

            {/* Price Chart */}
            {selectedSymbol && (
              <div className="mt-6">
                <PriceChart ticker={selectedSymbol} />
              </div>
            )}
          </div>

          {/* Alerts Panel */}
          <div className="lg:col-span-1">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900">
                Recent Alerts
              </h2>
              {alerts.some((a) => !a.isRead) && (
                <button
                  onClick={handleMarkAllRead}
                  className="text-sm text-blue-600 hover:text-blue-800"
                >
                  Mark all as read
                </button>
              )}
            </div>
            <AlertList alerts={alerts} />
          </div>
        </div>
      </main>
    </div>
  );
}

