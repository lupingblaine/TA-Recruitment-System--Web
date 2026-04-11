import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent } from '../../components/ui/Card';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import { Users, Briefcase, TrendingUp, AlertTriangle } from 'lucide-react';

const areaData = [
  { name: 'Week 1', applicants: 45 },
  { name: 'Week 2', applicants: 82 },
  { name: 'Week 3', applicants: 120 },
  { name: 'Week 4', applicants: 250 },
  { name: 'Week 5', applicants: 310 },
  { name: 'Week 6', applicants: 342 },
];

const barData = [
  { name: 'CS101', positions: 8, applicants: 120 },
  { name: 'CS201', positions: 5, applicants: 85 },
  { name: 'CS301', positions: 3, applicants: 40 },
  { name: 'CS405', positions: 4, applicants: 65 },
  { name: 'CS502', positions: 2, applicants: 32 },
];

export function AdminDashboard() {
  const stats = [
    { label: "Total Applicants", value: "342", increase: "+12%", icon: Users },
    { label: "Active Postings", value: "24", increase: "+4", icon: Briefcase },
    { label: "Offers Accepted", value: "85%", increase: "+2%", icon: TrendingUp },
  ];

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Platform Analytics</h1>
        <p className="text-zinc-500 mt-2">Overview of recruitment metrics and system status.</p>
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
              <CardContent className="p-6">
                <div className="flex justify-between items-start">
                  <div>
                    <p className="text-sm font-medium text-zinc-500">{stat.label}</p>
                    <p className="text-3xl font-bold text-zinc-900 dark:text-white mt-2">{stat.value}</p>
                  </div>
                  <div className="h-10 w-10 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-900 dark:text-zinc-50">
                    <stat.icon className="h-5 w-5" />
                  </div>
                </div>
                <div className="mt-4 flex items-center text-sm">
                  <span className="text-emerald-500 font-medium">{stat.increase}</span>
                  <span className="text-zinc-500 ml-2">from last month</span>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <Card className="p-6">
          <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-6">Application Trends</h3>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={areaData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorApplicants" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#18181b" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#18181b" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e4e4e7" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#71717a' }} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#71717a' }} />
                <Tooltip 
                  contentStyle={{ borderRadius: '12px', border: '1px solid #e4e4e7', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                  itemStyle={{ color: '#18181b', fontWeight: 600 }}
                />
                <Area type="monotone" dataKey="applicants" stroke="#18181b" strokeWidth={2} fillOpacity={1} fill="url(#colorApplicants)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card className="p-6">
          <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-6">Applicants per Module</h3>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e4e4e7" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#71717a' }} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#71717a' }} />
                <Tooltip 
                  contentStyle={{ borderRadius: '12px', border: '1px solid #e4e4e7', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                  cursor={{ fill: '#f4f4f5' }}
                />
                <Bar dataKey="applicants" fill="#18181b" radius={[4, 4, 0, 0]} barSize={40} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      <Card className="p-6 bg-red-50/50 border-red-100 dark:bg-red-950/20 dark:border-red-900/50">
        <div className="flex items-start gap-4">
          <div className="h-10 w-10 rounded-full bg-red-100 dark:bg-red-900/50 flex items-center justify-center shrink-0">
            <AlertTriangle className="h-5 w-5 text-red-600 dark:text-red-400" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-red-900 dark:text-red-400">System Alerts</h3>
            <p className="text-sm text-red-700/80 dark:text-red-300 mt-1">2 modules (CS502, CS601) have unfulfilled TA positions past their target deadline. Intervention may be required.</p>
          </div>
        </div>
      </Card>
    </div>
  );
}
