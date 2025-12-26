import { Alert } from '../services/api';
import { alertsApi } from '../services/api';
import {
  AlertTriangle,
  TrendingUp,
  TrendingDown,
  Activity,
  BarChart3,
  CheckCircle,
} from 'lucide-react';

interface AlertListProps {
  alerts: Alert[];
}

export default function AlertList({ alerts }: AlertListProps) {
  const getAlertIcon = (type: string) => {
    switch (type) {
      case 'SPIKE_UP':
        return <TrendingUp className="h-5 w-5 text-green-500" />;
      case 'SPIKE_DOWN':
        return <TrendingDown className="h-5 w-5 text-red-500" />;
      case 'VOLUME_SURGE':
        return <BarChart3 className="h-5 w-5 text-blue-500" />;
      case 'ANOMALY':
        return <AlertTriangle className="h-5 w-5 text-yellow-500" />;
      default:
        return <Activity className="h-5 w-5 text-gray-500" />;
    }
  };

  const getAlertBgColor = (type: string, isRead: boolean) => {
    if (isRead) return 'bg-gray-50';
    switch (type) {
      case 'SPIKE_UP':
        return 'bg-green-50 border-l-4 border-green-500';
      case 'SPIKE_DOWN':
        return 'bg-red-50 border-l-4 border-red-500';
      case 'VOLUME_SURGE':
        return 'bg-blue-50 border-l-4 border-blue-500';
      case 'ANOMALY':
        return 'bg-yellow-50 border-l-4 border-yellow-500';
      default:
        return 'bg-gray-50';
    }
  };

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return date.toLocaleDateString();
  };

  const handleMarkRead = async (id: number) => {
    try {
      await alertsApi.markAsRead(id);
    } catch (error) {
      console.error('Failed to mark alert as read:', error);
    }
  };

  if (alerts.length === 0) {
    return (
      <div className="bg-white rounded-xl shadow-md p-6 text-center">
        <CheckCircle className="h-12 w-12 text-green-500 mx-auto mb-3" />
        <p className="text-gray-500">No recent alerts</p>
        <p className="text-sm text-gray-400 mt-1">
          You'll be notified of significant market movements
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-md overflow-hidden">
      <div className="max-h-[600px] overflow-y-auto">
        {alerts.map((alert, index) => (
          <div
            key={`${alert.id}-${index}`}
            className={`p-4 ${getAlertBgColor(alert.alertType, alert.isRead)} ${
              index !== alerts.length - 1 ? 'border-b border-gray-100' : ''
            } transition-colors hover:bg-gray-100`}
            onClick={() => !alert.isRead && handleMarkRead(alert.id)}
          >
            <div className="flex items-start space-x-3">
              <div className="flex-shrink-0 mt-0.5">
                {getAlertIcon(alert.alertType)}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-semibold text-gray-900">
                    {alert.ticker}
                  </span>
                  <span className="text-xs text-gray-500">
                    {formatTime(alert.timestamp)}
                  </span>
                </div>
                <p className="text-sm text-gray-600 mt-1">{alert.message}</p>
                {alert.triggerValue && (
                  <p className="text-xs text-gray-500 mt-1">
                    Price: ${alert.triggerValue.toLocaleString()}
                  </p>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

