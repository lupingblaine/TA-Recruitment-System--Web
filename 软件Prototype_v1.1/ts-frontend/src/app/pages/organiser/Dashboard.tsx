import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Link } from 'react-router';
import { Users, Briefcase, Plus, Calendar, Clock, ChevronRight } from 'lucide-react';

export function OrganiserDashboard() {
  const stats = [
    { label: "Active Postings", value: "3", icon: Briefcase },
    { label: "Total Applicants", value: "142", icon: Users },
    { label: "Interviews to Schedule", value: "8", icon: Calendar },
  ];

  const recentApplications = [
    { id: 1, name: 'Alex Johnson', role: 'CS201 Data Structures', status: 'Review', date: '2 hours ago' },
    { id: 2, name: 'Sarah Lee', role: 'CS405 Machine Learning', status: 'Shortlisted', date: '5 hours ago' },
    { id: 3, name: 'Michael Chen', role: 'CS101 Intro to Programming', status: 'Interview', date: '1 day ago' },
  ];

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Organiser Overview</h1>
          <p className="text-zinc-500 mt-2">Manage your TA recruitment pipeline across modules.</p>
        </div>
        <Link to="/organiser/jobs/create">
          <Button className="gap-2 shrink-0">
            <Plus className="h-4 w-4" /> Create Posting
          </Button>
        </Link>
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
            <h2 className="text-xl font-semibold text-zinc-900 dark:text-white">Recent Activity</h2>
            <Link to="/organiser/pipeline">
              <Button variant="ghost" size="sm" className="text-zinc-500 hover:text-zinc-900">
                View pipeline
              </Button>
            </Link>
          </div>
          
          <Card>
            <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
              {recentApplications.map((app) => (
                <div key={app.id} className="p-6 flex items-center justify-between hover:bg-zinc-50/50 dark:hover:bg-zinc-800/20 transition-colors cursor-pointer group">
                  <div className="flex items-center gap-4">
                    <div className="h-10 w-10 rounded-full bg-zinc-200 dark:bg-zinc-800 flex items-center justify-center font-medium text-zinc-600 dark:text-zinc-300">
                      {app.name.charAt(0)}
                    </div>
                    <div className="space-y-1">
                      <h3 className="font-semibold text-zinc-900 dark:text-white group-hover:text-blue-600 transition-colors">{app.name}</h3>
                      <p className="text-sm text-zinc-500">Applied for {app.role}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4 text-right">
                    <div className="flex flex-col items-end gap-1">
                      <Badge variant={app.status === 'Shortlisted' ? 'success' : app.status === 'Interview' ? 'warning' : 'neutral'}>
                        {app.status}
                      </Badge>
                      <span className="text-xs text-zinc-500 flex items-center gap-1">
                        <Clock className="h-3 w-3" /> {app.date}
                      </span>
                    </div>
                    <ChevronRight className="h-5 w-5 text-zinc-300 group-hover:text-zinc-500 transition-colors hidden sm:block" />
                  </div>
                </div>
              ))}
            </div>
            <div className="p-4 border-t border-zinc-100 dark:border-zinc-800 text-center">
               <Link to="/organiser/pipeline" className="text-sm font-medium text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white">View all activity &rarr;</Link>
            </div>
          </Card>
        </div>

        <div className="space-y-6">
          <h2 className="text-xl font-semibold text-zinc-900 dark:text-white">Active Postings</h2>
          <Card>
            <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
              {[
                { module: 'CS201', applicants: 45, positions: 3, deadline: 'In 3 days' },
                { module: 'CS405', applicants: 32, positions: 2, deadline: 'In 5 days' },
                { module: 'CS101', applicants: 89, positions: 5, deadline: 'In 10 days' },
              ].map((posting, i) => (
                <div key={i} className="p-6">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-semibold text-zinc-900 dark:text-white">{posting.module}</h3>
                    <Badge variant="outline" className="text-amber-600 border-amber-200 bg-amber-50 dark:border-amber-900/50 dark:bg-amber-900/20">{posting.deadline}</Badge>
                  </div>
                  <div className="flex items-center gap-4 text-sm mt-3">
                    <div className="flex flex-col">
                      <span className="text-zinc-500">Applicants</span>
                      <span className="font-semibold text-zinc-900 dark:text-white">{posting.applicants}</span>
                    </div>
                    <div className="h-8 w-px bg-zinc-200 dark:bg-zinc-800" />
                    <div className="flex flex-col">
                      <span className="text-zinc-500">Positions</span>
                      <span className="font-semibold text-zinc-900 dark:text-white">{posting.positions}</span>
                    </div>
                  </div>
                  <div className="mt-4 w-full h-1.5 bg-zinc-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                    <div className="h-full bg-blue-500 rounded-full" style={{ width: `${(posting.applicants / 100) * 100}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
