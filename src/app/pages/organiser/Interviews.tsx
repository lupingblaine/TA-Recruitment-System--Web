import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Calendar, Clock, Video, Check, X, User } from 'lucide-react';

export function OrganiserInterviews() {
  const interviews = [
    {
      id: 1,
      candidate: 'Emma Wilson',
      module: 'CS201 Data Structures',
      date: 'Today, Oct 15',
      time: '10:00 AM - 10:30 AM',
      type: 'Video Call',
      status: 'upcoming'
    },
    {
      id: 2,
      candidate: 'David Kim',
      module: 'CS405 Machine Learning',
      date: 'Today, Oct 15',
      time: '11:00 AM - 11:45 AM',
      type: 'Video Call',
      status: 'upcoming'
    },
    {
      id: 3,
      candidate: 'Lisa Wang',
      module: 'CS101 Intro to Programming',
      date: 'Tomorrow, Oct 16',
      time: '02:00 PM - 02:30 PM',
      type: 'In Person - Room 402',
      status: 'scheduled'
    }
  ];

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Interviews</h1>
          <p className="text-zinc-500 mt-2">Manage your upcoming candidate interviews.</p>
        </div>
        <Button className="gap-2 shrink-0">
          <Calendar className="h-4 w-4" /> Schedule Interview
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div className="md:col-span-2 space-y-6">
          <h2 className="text-xl font-semibold text-zinc-900 dark:text-white">Upcoming Sessions</h2>
          
          <div className="space-y-4">
            {interviews.map((interview, i) => (
              <motion.div
                key={interview.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.1 }}
              >
                <Card className={`border-l-4 ${interview.date.includes('Today') ? 'border-l-blue-500' : 'border-l-zinc-300 dark:border-l-zinc-700'}`}>
                  <CardContent className="p-6">
                    <div className="flex flex-col sm:flex-row justify-between gap-4">
                      <div className="space-y-3">
                        <div className="flex items-center gap-3">
                          <div className="h-10 w-10 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center font-bold text-zinc-700 dark:text-zinc-300">
                            {interview.candidate.charAt(0)}
                          </div>
                          <div>
                            <h3 className="font-bold text-zinc-900 dark:text-white">{interview.candidate}</h3>
                            <p className="text-sm text-zinc-500">{interview.module}</p>
                          </div>
                        </div>

                        <div className="flex flex-wrap items-center gap-4 text-sm text-zinc-600 dark:text-zinc-400">
                          <span className="flex items-center gap-1.5 font-medium text-zinc-900 dark:text-zinc-100">
                            <Calendar className="h-4 w-4 text-zinc-400" /> {interview.date}
                          </span>
                          <span className="flex items-center gap-1.5">
                            <Clock className="h-4 w-4 text-zinc-400" /> {interview.time}
                          </span>
                          <span className="flex items-center gap-1.5">
                            {interview.type.includes('Video') ? <Video className="h-4 w-4 text-zinc-400" /> : <User className="h-4 w-4 text-zinc-400" />} 
                            {interview.type}
                          </span>
                        </div>
                      </div>

                      <div className="flex flex-row sm:flex-col justify-end gap-2 shrink-0">
                        {interview.type.includes('Video') && (
                          <Button variant="outline" size="sm" className="w-full sm:w-auto text-blue-600 border-blue-200 hover:bg-blue-50 dark:border-blue-900/50 dark:text-blue-400 dark:hover:bg-blue-900/20">
                            Join Call
                          </Button>
                        )}
                        <Button variant="ghost" size="sm" className="w-full sm:w-auto">
                          Reschedule
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>

        <div className="space-y-6">
          <h2 className="text-xl font-semibold text-zinc-900 dark:text-white">Action Required</h2>
          <Card className="bg-amber-50/50 border-amber-100 dark:bg-amber-950/20 dark:border-amber-900/50">
            <CardContent className="p-6 space-y-4">
              <div className="space-y-2">
                <Badge variant="warning">Needs Outcome</Badge>
                <h3 className="font-semibold text-zinc-900 dark:text-white">Alex Johnson</h3>
                <p className="text-sm text-zinc-600 dark:text-zinc-400">Interviewed yesterday for CS201.</p>
              </div>
              
              <div className="flex gap-2 pt-2">
                <Button size="sm" variant="outline" className="w-full border-emerald-200 text-emerald-700 hover:bg-emerald-50 dark:border-emerald-900/50 dark:text-emerald-400 dark:hover:bg-emerald-900/20 gap-1.5">
                  <Check className="h-3.5 w-3.5" /> Offer
                </Button>
                <Button size="sm" variant="outline" className="w-full border-red-200 text-red-700 hover:bg-red-50 dark:border-red-900/50 dark:text-red-400 dark:hover:bg-red-900/20 gap-1.5">
                  <X className="h-3.5 w-3.5" /> Reject
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
