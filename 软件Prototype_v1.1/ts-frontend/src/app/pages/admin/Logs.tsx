import React, { useState } from 'react';
import { motion } from 'motion/react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Download, Filter, Search, Activity, AlertCircle, CheckCircle2, Info, Settings, ShieldAlert, FileText } from 'lucide-react';

export function AdminLogs() {
  const [searchQuery, setSearchQuery] = useState('');

  const logs = [
    { id: 'EVT-9321', timestamp: '2026-10-15 14:32:01', action: 'User Login', actor: 'alan@university.edu', type: 'info', details: 'Successful authentication via SSO.' },
    { id: 'EVT-9320', timestamp: '2026-10-15 13:15:44', action: 'Job Posted', actor: 'grace@university.edu', type: 'success', details: 'Created new TA position for CS201.' },
    { id: 'EVT-9319', timestamp: '2026-10-15 11:05:12', action: 'Role Update', actor: 'john@university.edu', type: 'warning', details: 'Elevated david@university.edu to Organiser role.' },
    { id: 'EVT-9318', timestamp: '2026-10-15 09:45:00', action: 'Failed Login', actor: 'unknown (IP: 192.168.1.45)', type: 'error', details: 'Invalid credentials. 3rd attempt.' },
    { id: 'EVT-9317', timestamp: '2026-10-14 16:20:11', action: 'Application Submitted', actor: 'emma@university.edu', type: 'info', details: 'Applied for CS101 Head TA position.' },
    { id: 'EVT-9316', timestamp: '2026-10-14 15:10:05', action: 'System Backup', actor: 'System', type: 'info', details: 'Automated database snapshot completed.' },
    { id: 'EVT-9315', timestamp: '2026-10-14 10:00:22', action: 'Settings Changed', actor: 'john@university.edu', type: 'warning', details: 'Updated Global Term Fall 2026 deadline.' },
  ];

  const filteredLogs = logs.filter(log => 
    log.action.toLowerCase().includes(searchQuery.toLowerCase()) || 
    log.actor.toLowerCase().includes(searchQuery.toLowerCase()) ||
    log.id.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getLogIcon = (type: string) => {
    switch (type) {
      case 'info': return <Info className="h-4 w-4 text-blue-500" />;
      case 'success': return <CheckCircle2 className="h-4 w-4 text-emerald-500" />;
      case 'warning': return <AlertCircle className="h-4 w-4 text-amber-500" />;
      case 'error': return <ShieldAlert className="h-4 w-4 text-red-500" />;
      default: return <Activity className="h-4 w-4 text-zinc-500" />;
    }
  };

  const getLogStyle = (type: string) => {
    switch (type) {
      case 'info': return 'bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400 border-blue-200 dark:border-blue-900/50';
      case 'success': return 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/50';
      case 'warning': return 'bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-400 border-amber-200 dark:border-amber-900/50';
      case 'error': return 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400 border-red-200 dark:border-red-900/50';
      default: return 'bg-zinc-50 text-zinc-700 dark:bg-zinc-900/20 dark:text-zinc-400 border-zinc-200 dark:border-zinc-800';
    }
  };

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">System Logs</h1>
          <p className="text-zinc-500 mt-2">Monitor platform activity, security events, and audit trails.</p>
        </div>
        <Button className="gap-2 shrink-0 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-50 dark:hover:bg-zinc-800">
          <Download className="h-4 w-4" /> Export CSV
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
          <input 
            type="text" 
            placeholder="Search events, actors, or IDs..." 
            className="w-full pl-10 pr-4 py-2 bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900/10 dark:focus:ring-white/10"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <div className="flex gap-2 w-full sm:w-auto overflow-x-auto pb-2 sm:pb-0">
          <Button variant="outline" size="sm" className="gap-1.5 shrink-0 h-9">
            <Filter className="h-3.5 w-3.5" /> All Types
          </Button>
          <Button variant="outline" size="sm" className="gap-1.5 shrink-0 h-9 border-red-200 text-red-600 hover:bg-red-50 dark:border-red-900/50 dark:text-red-400">
            <ShieldAlert className="h-3.5 w-3.5" /> Errors
          </Button>
        </div>
      </div>

      <Card className="overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="text-xs text-zinc-500 bg-zinc-50/50 dark:bg-zinc-900/50 border-b border-zinc-200 dark:border-zinc-800 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-4 font-medium">Event ID / Time</th>
                <th className="px-6 py-4 font-medium">Action</th>
                <th className="px-6 py-4 font-medium">Actor</th>
                <th className="px-6 py-4 font-medium w-1/3">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {filteredLogs.map((log, i) => (
                <motion.tr 
                  key={log.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.05 }}
                  className="hover:bg-zinc-50/50 dark:hover:bg-zinc-900/50 transition-colors"
                >
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex flex-col gap-1">
                      <span className="font-mono text-xs text-zinc-900 dark:text-white font-medium">{log.id}</span>
                      <span className="text-zinc-500 text-xs">{log.timestamp}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className={`inline-flex items-center gap-2 px-2.5 py-1 rounded-md border text-xs font-medium ${getLogStyle(log.type)}`}>
                      {getLogIcon(log.type)}
                      {log.action}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-zinc-600 dark:text-zinc-400 font-mono text-xs">
                    {log.actor}
                  </td>
                  <td className="px-6 py-4 text-zinc-600 dark:text-zinc-400 text-sm">
                    {log.details}
                  </td>
                </motion.tr>
              ))}
            </tbody>
          </table>
        </div>
        {filteredLogs.length === 0 && (
          <div className="p-8 text-center text-zinc-500 flex flex-col items-center gap-3">
            <FileText className="h-8 w-8 text-zinc-300 dark:text-zinc-700" />
            <p>No audit logs found matching your criteria.</p>
          </div>
        )}
      </Card>
    </div>
  );
}
