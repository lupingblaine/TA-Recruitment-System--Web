import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Calendar, Clock, ChevronRight, CheckCircle2, TrendingUp, Info } from 'lucide-react';

export function ApplicantDashboard() {
  const stats = [
    { label: "Active Applications", value: "3", icon: Clock },
    { label: "Interviews Scheduled", value: "1", icon: Calendar },
    { label: "Profile Completion", value: "85%", icon: CheckCircle2 },
  ];

  const applications = [
    { id: 1, role: 'Data Structures TA', module: 'CS201', status: 'Under Review', date: '2 days ago', color: 'warning' as const },
    { id: 2, role: 'Machine Learning Lab Assistant', module: 'CS405', status: 'Interview', date: '1 week ago', color: 'success' as const },
    { id: 3, role: 'Intro to Programming TA', module: 'CS101', status: 'Applied', date: '2 weeks ago', color: 'neutral' as const },
  ];

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Welcome back, Alex!</h1>
        <p className="text-zinc-500 mt-2 text-lg">Here's what's happening with your TA applications.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {stats.map((stat, i) => (
          <motion.div
            key={i}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.1 }}
          >
            <Card>
              <CardContent className="p-6 flex items-center gap-4">
                <div className="h-12 w-12 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-900 dark:text-zinc-50">
                  <stat.icon className="h-6 w-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-zinc-500">{stat.label}</p>
                  <p className="text-2xl font-bold text-zinc-900 dark:text-white">{stat.value}</p>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-zinc-900 dark:text-white">Recent Applications</h2>
            <Button variant="ghost" size="sm" className="text-zinc-500 hover:text-zinc-900">
              View all
            </Button>
          </div>
          
          <Card>
            <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
              {applications.map((app) => (
                <div key={app.id} className="p-6 flex items-center justify-between hover:bg-zinc-50/50 dark:hover:bg-zinc-800/20 transition-colors cursor-pointer group">
                  <div className="space-y-1">
                    <h3 className="font-semibold text-zinc-900 dark:text-white group-hover:text-blue-600 transition-colors">{app.role}</h3>
                    <p className="text-sm text-zinc-500">{app.module} • Applied {app.date}</p>
                  </div>
                  <div className="flex items-center gap-4">
                    <Badge variant={app.color}>{app.status}</Badge>
                    <ChevronRight className="h-5 w-5 text-zinc-300 group-hover:text-zinc-500 transition-colors" />
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>

        <div className="space-y-6">
          <h2 className="text-xl font-semibold text-zinc-900 dark:text-white">Your Workload Tracker</h2>
          <Card className="bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-indigo-950/20 dark:to-purple-950/20 border-indigo-100 dark:border-indigo-900/30">
            <CardContent className="p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5 text-indigo-600 dark:text-indigo-400" />
                  <span className="font-semibold text-indigo-900 dark:text-indigo-100">Fall 2026 Term</span>
                </div>
                <Badge variant="neutral" className="bg-white/50 dark:bg-black/20 text-indigo-700 dark:text-indigo-300 border-indigo-200 dark:border-indigo-800">Healthy Load</Badge>
              </div>
              
              <div className="space-y-2 mb-4">
                <div className="flex justify-between text-sm">
                  <span className="text-indigo-800/70 dark:text-indigo-200/70">Estimated Commitment</span>
                  <span className="font-bold text-indigo-900 dark:text-indigo-100">12 / 20 hrs</span>
                </div>
                <div className="h-2 w-full bg-indigo-200/50 dark:bg-indigo-900/50 rounded-full overflow-hidden">
                  <motion.div 
                    initial={{ width: 0 }}
                    animate={{ width: "60%" }}
                    transition={{ duration: 1, ease: "easeOut" }}
                    className="h-full bg-indigo-500 rounded-full"
                  />
                </div>
              </div>
              
              <div className="bg-white/60 dark:bg-black/20 rounded-lg p-3 text-sm flex gap-3 text-indigo-900 dark:text-indigo-200">
                <Info className="h-5 w-5 shrink-0 text-indigo-500" />
                <p>You can safely take on <strong>1 more standard TA module</strong> (approx 8 hrs/week) before reaching the recommended maximum.</p>
              </div>
            </CardContent>
          </Card>

          <h2 className="text-xl font-semibold text-zinc-900 dark:text-white mt-8">Upcoming Deadlines</h2>
          <Card>
            <CardContent className="p-6 space-y-6">
              {[
                { module: 'CS302 DB Systems', date: 'Tomorrow, 11:59 PM', type: 'Application close' },
                { module: 'CS405 Machine Learning', date: 'Friday, 10:00 AM', type: 'Interview' },
              ].map((item, i) => (
                <div key={i} className="flex gap-4">
                  <div className="mt-0.5 h-2 w-2 rounded-full bg-blue-500 shrink-0" />
                  <div>
                    <p className="font-medium text-zinc-900 dark:text-white text-sm">{item.module}</p>
                    <p className="text-sm text-zinc-500 mt-1">{item.type} • {item.date}</p>
                  </div>
                </div>
              ))}
              <Button variant="outline" className="w-full mt-4">Browse more jobs</Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
