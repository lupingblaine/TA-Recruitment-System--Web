import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Search, Filter, AlertTriangle, CheckCircle2, TrendingUp, Info } from 'lucide-react';

export function AdminWorkload() {
  const [searchQuery, setSearchQuery] = useState('');

  const tas = [
    { id: 1, name: 'Alex Johnson', department: 'Computer Science', assignedModules: ['CS101', 'CS201'], currentHours: 18, maxHours: 20, status: 'optimal' },
    { id: 2, name: 'Sarah Lee', department: 'Mathematics', assignedModules: ['MTH100', 'MTH201', 'MTH301'], currentHours: 24, maxHours: 20, status: 'overloaded' },
    { id: 3, name: 'Michael Chen', department: 'Computer Science', assignedModules: ['CS405'], currentHours: 8, maxHours: 20, status: 'light' },
    { id: 4, name: 'Emma Wilson', department: 'Physics', assignedModules: ['PHY101', 'PHY102'], currentHours: 20, maxHours: 20, status: 'at-capacity' },
    { id: 5, name: 'David Kim', department: 'Computer Science', assignedModules: ['CS101', 'CS301'], currentHours: 15, maxHours: 20, status: 'optimal' },
    { id: 6, name: 'Lisa Wang', department: 'Data Science', assignedModules: ['DS200'], currentHours: 5, maxHours: 15, status: 'light' },
  ];

  const filteredTAs = tas.filter(ta => 
    ta.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    ta.department.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'overloaded': return 'text-red-600 bg-red-50 dark:bg-red-900/20 dark:text-red-400 border-red-200 dark:border-red-900/50';
      case 'at-capacity': return 'text-amber-600 bg-amber-50 dark:bg-amber-900/20 dark:text-amber-400 border-amber-200 dark:border-amber-900/50';
      case 'optimal': return 'text-emerald-600 bg-emerald-50 dark:bg-emerald-900/20 dark:text-emerald-400 border-emerald-200 dark:border-emerald-900/50';
      case 'light': return 'text-blue-600 bg-blue-50 dark:bg-blue-900/20 dark:text-blue-400 border-blue-200 dark:border-blue-900/50';
      default: return 'text-zinc-600 bg-zinc-50 border-zinc-200';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'overloaded': return 'Overloaded';
      case 'at-capacity': return 'At Capacity';
      case 'optimal': return 'Optimal';
      case 'light': return 'Light Load';
      default: return status;
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'overloaded': return <AlertTriangle className="h-4 w-4" />;
      case 'at-capacity': return <Info className="h-4 w-4" />;
      case 'optimal': return <CheckCircle2 className="h-4 w-4" />;
      case 'light': return <TrendingUp className="h-4 w-4" />;
      default: return null;
    }
  };

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white flex items-center gap-2">
            Workload Intelligence <Badge className="bg-gradient-to-r from-indigo-500 to-purple-500 text-white border-0">AI Assisted</Badge>
          </h1>
          <p className="text-zinc-500 mt-2">Monitor TA workload distribution and automatically prevent burnout or unfair allocation.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="p-4 bg-gradient-to-br from-zinc-50 to-white dark:from-zinc-900 dark:to-zinc-950 border border-zinc-200 dark:border-zinc-800">
          <p className="text-sm font-medium text-zinc-500 mb-1">Total Active TAs</p>
          <p className="text-2xl font-bold text-zinc-900 dark:text-white">124</p>
        </Card>
        <Card className="p-4 bg-gradient-to-br from-red-50 to-white dark:from-red-900/10 dark:to-zinc-950 border border-red-100 dark:border-red-900/30">
          <p className="text-sm font-medium text-red-600 dark:text-red-400 mb-1">Overloaded TAs</p>
          <div className="flex items-center justify-between">
            <p className="text-2xl font-bold text-red-700 dark:text-red-500">12</p>
            <Badge variant="neutral" className="bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400 border-0">Requires Action</Badge>
          </div>
        </Card>
        <Card className="p-4 bg-gradient-to-br from-emerald-50 to-white dark:from-emerald-900/10 dark:to-zinc-950 border border-emerald-100 dark:border-emerald-900/30">
          <p className="text-sm font-medium text-emerald-600 dark:text-emerald-400 mb-1">Optimally Loaded</p>
          <p className="text-2xl font-bold text-emerald-700 dark:text-emerald-500">85</p>
        </Card>
        <Card className="p-4 bg-gradient-to-br from-blue-50 to-white dark:from-blue-900/10 dark:to-zinc-950 border border-blue-100 dark:border-blue-900/30">
          <p className="text-sm font-medium text-blue-600 dark:text-blue-400 mb-1">Available Capacity</p>
          <p className="text-2xl font-bold text-blue-700 dark:text-blue-500">27</p>
        </Card>
      </div>

      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
          <input 
            type="text" 
            placeholder="Search TAs by name or department..." 
            className="w-full pl-10 pr-4 py-2 bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900/10 dark:focus:ring-white/10"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <Button variant="outline" className="gap-2 shrink-0 w-full sm:w-auto">
          <Filter className="h-4 w-4" /> Filter by Status
        </Button>
      </div>

      <div className="space-y-4">
        <AnimatePresence>
          {filteredTAs.map((ta, i) => (
            <motion.div
              key={ta.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.05 }}
            >
              <Card className={`p-5 overflow-hidden border ${ta.status === 'overloaded' ? 'border-red-200 dark:border-red-900/50 shadow-sm shadow-red-100 dark:shadow-red-900/20' : 'border-zinc-200 dark:border-zinc-800'}`}>
                <div className="flex flex-col lg:flex-row gap-6 items-start lg:items-center justify-between">
                  <div className="flex items-center gap-4 min-w-[250px]">
                    <div className={`h-12 w-12 rounded-full flex items-center justify-center font-bold text-lg border ${
                      ta.status === 'overloaded' ? 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/30 dark:text-red-400' : 'bg-zinc-100 text-zinc-700 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-300'
                    }`}>
                      {ta.name.charAt(0)}
                    </div>
                    <div>
                      <h3 className="font-bold text-zinc-900 dark:text-white">{ta.name}</h3>
                      <p className="text-sm text-zinc-500">{ta.department}</p>
                    </div>
                  </div>

                  <div className="flex-1 w-full max-w-2xl">
                    <div className="flex items-center justify-between text-sm mb-2">
                      <span className="font-medium text-zinc-700 dark:text-zinc-300">Workload Capacity</span>
                      <span className="font-medium text-zinc-900 dark:text-white">{ta.currentHours} / {ta.maxHours} hrs/week</span>
                    </div>
                    <div className="h-3 w-full bg-zinc-100 dark:bg-zinc-800 rounded-full overflow-hidden flex">
                      <motion.div 
                        initial={{ width: 0 }}
                        animate={{ width: `${Math.min((ta.currentHours / ta.maxHours) * 100, 100)}%` }}
                        transition={{ duration: 1, ease: "easeOut" }}
                        className={`h-full ${
                          ta.status === 'overloaded' ? 'bg-red-500' : 
                          ta.status === 'at-capacity' ? 'bg-amber-500' : 
                          ta.status === 'optimal' ? 'bg-emerald-500' : 'bg-blue-500'
                        }`}
                      />
                      {ta.currentHours > ta.maxHours && (
                        <motion.div 
                          initial={{ width: 0 }}
                          animate={{ width: `${Math.min(((ta.currentHours - ta.maxHours) / ta.maxHours) * 100, 100)}%` }}
                          transition={{ duration: 1, delay: 0.5, ease: "easeOut" }}
                          className="h-full bg-red-600/80 pattern-diagonal-lines pattern-zinc-900 pattern-bg-transparent pattern-size-1 pattern-opacity-20"
                        />
                      )}
                    </div>
                    
                    <div className="flex flex-wrap items-center gap-2 mt-3">
                      {ta.assignedModules.map(mod => (
                        <span key={mod} className="text-xs px-2 py-1 bg-zinc-100 dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300 rounded-md border border-zinc-200 dark:border-zinc-700">
                          {mod}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 min-w-[200px] justify-end w-full lg:w-auto">
                    <div className={`px-3 py-1.5 rounded-full text-xs font-semibold flex items-center gap-1.5 border ${getStatusColor(ta.status)}`}>
                      {getStatusIcon(ta.status)}
                      {getStatusLabel(ta.status)}
                    </div>
                    {ta.status === 'overloaded' && (
                      <Button size="sm" variant="outline" className="text-red-600 border-red-200 hover:bg-red-50 dark:border-red-900/50 dark:hover:bg-red-900/20">
                        Reassign
                      </Button>
                    )}
                  </div>
                </div>
              </Card>
            </motion.div>
          ))}
        </AnimatePresence>
        
        {filteredTAs.length === 0 && (
          <div className="text-center py-12 text-zinc-500">
            No TAs found matching your criteria.
          </div>
        )}
      </div>
    </div>
  );
}
