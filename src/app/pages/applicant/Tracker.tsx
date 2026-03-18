import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Check, Clock, X, Calendar, MessageSquare, ChevronDown } from 'lucide-react';

export function ApplicationTracker() {
  const applications = [
    {
      id: 1,
      role: 'Teaching Assistant - Data Structures',
      module: 'CS201',
      date: 'Oct 1, 2026',
      status: 'interview', // applied, review, shortlisted, interview, offer, rejected
      currentStep: 3,
      timeline: [
        { status: 'Applied', date: 'Oct 1, 2026', completed: true },
        { status: 'Under Review', date: 'Oct 5, 2026', completed: true },
        { status: 'Shortlisted', date: 'Oct 10, 2026', completed: true },
        { status: 'Interview', date: 'Oct 15, 2026', completed: false, pending: true },
        { status: 'Offer / Rejected', date: 'TBD', completed: false },
      ]
    },
    {
      id: 2,
      role: 'Lab Assistant - Machine Learning',
      module: 'CS405',
      date: 'Sep 25, 2026',
      status: 'review',
      currentStep: 1,
      timeline: [
        { status: 'Applied', date: 'Sep 25, 2026', completed: true },
        { status: 'Under Review', date: 'In Progress', completed: false, pending: true },
        { status: 'Shortlisted', date: 'TBD', completed: false },
        { status: 'Interview', date: 'TBD', completed: false },
        { status: 'Offer / Rejected', date: 'TBD', completed: false },
      ]
    },
    {
      id: 3,
      role: 'Head TA - Intro to Programming',
      module: 'CS101',
      date: 'Sep 10, 2026',
      status: 'rejected',
      currentStep: 4,
      timeline: [
        { status: 'Applied', date: 'Sep 10, 2026', completed: true },
        { status: 'Under Review', date: 'Sep 12, 2026', completed: true },
        { status: 'Shortlisted', date: 'Sep 18, 2026', completed: true },
        { status: 'Interview', date: 'Sep 22, 2026', completed: true },
        { status: 'Rejected', date: 'Sep 28, 2026', completed: true, error: true },
      ]
    },
  ];

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Application Tracker</h1>
        <p className="text-zinc-500 mt-2">Track the progress of your submitted TA applications.</p>
      </div>

      <div className="space-y-6">
        {applications.map((app, index) => (
          <motion.div
            key={app.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
          >
            <Card className="overflow-hidden shadow-sm hover:shadow-md transition-shadow dark:border-zinc-800">
              <div className="p-6 sm:p-8 bg-white dark:bg-zinc-950 border-b border-zinc-200 dark:border-zinc-800 flex flex-col md:flex-row justify-between items-start gap-6">
                <div className="space-y-2">
                  <div className="flex items-center gap-3">
                    <Badge variant={app.status === 'rejected' ? 'danger' : app.status === 'interview' ? 'warning' : 'neutral'}>
                      {app.status === 'rejected' ? 'Rejected' : app.status === 'interview' ? 'Action Required' : 'In Progress'}
                    </Badge>
                    <span className="text-sm font-medium text-zinc-500">Applied {app.date}</span>
                  </div>
                  <h3 className="text-xl font-bold text-zinc-900 dark:text-white mt-2">{app.role}</h3>
                  <p className="text-zinc-500">{app.module}</p>
                </div>
                
                {app.status === 'interview' && (
                  <div className="flex flex-col gap-2 w-full md:w-auto">
                    <div className="bg-amber-50 dark:bg-amber-900/20 text-amber-800 dark:text-amber-500 p-3 rounded-lg border border-amber-200 dark:border-amber-900/50 flex items-start gap-3">
                      <Calendar className="h-5 w-5 shrink-0 mt-0.5" />
                      <div>
                        <p className="text-sm font-semibold">Interview Invitation</p>
                        <p className="text-xs mt-1">Please schedule your interview with Dr. Turing.</p>
                      </div>
                    </div>
                    <Button size="sm" className="w-full">Schedule Now</Button>
                  </div>
                )}
                
                {app.status === 'rejected' && (
                  <Button variant="outline" size="sm" className="w-full md:w-auto">View Feedback</Button>
                )}
              </div>

              <CardContent className="p-6 sm:p-8 bg-zinc-50/50 dark:bg-zinc-900/20">
                <div className="relative">
                  {/* Timeline connecting line */}
                  <div className="absolute top-4 left-0 w-full h-0.5 bg-zinc-200 dark:bg-zinc-800 hidden md:block" />
                  <div 
                    className="absolute top-4 left-0 h-0.5 bg-zinc-900 dark:bg-zinc-50 transition-all duration-1000 ease-out hidden md:block"
                    style={{ width: `${(app.currentStep / (app.timeline.length - 1)) * 100}%` }}
                  />

                  <div className="flex flex-col md:flex-row justify-between relative z-10 gap-8 md:gap-4">
                    {app.timeline.map((step, i) => (
                      <div key={i} className="flex md:flex-col items-center gap-4 md:gap-2 w-full md:w-auto relative">
                        {/* Mobile vertical line */}
                        {i !== app.timeline.length - 1 && (
                          <div className="absolute left-[15px] top-8 bottom-[-32px] w-0.5 bg-zinc-200 dark:bg-zinc-800 md:hidden" />
                        )}
                        {i < app.currentStep && (
                          <div className="absolute left-[15px] top-8 bottom-[-32px] w-0.5 bg-zinc-900 dark:bg-zinc-50 md:hidden" />
                        )}

                        <div className={`
                          h-8 w-8 rounded-full flex items-center justify-center shrink-0 border-2 transition-colors z-10 bg-white dark:bg-zinc-950
                          ${step.error ? 'border-red-500 bg-red-500 text-white' : 
                            step.completed ? 'border-zinc-900 bg-zinc-900 text-white dark:border-zinc-50 dark:bg-zinc-50 dark:text-zinc-900' : 
                            step.pending ? 'border-zinc-900 text-zinc-900 dark:border-zinc-50 dark:text-zinc-50' : 
                            'border-zinc-200 text-zinc-400 dark:border-zinc-800 dark:text-zinc-600'}
                        `}>
                          {step.error ? <X className="h-4 w-4" /> :
                           step.completed ? <Check className="h-4 w-4" /> : 
                           step.pending ? <div className="h-2.5 w-2.5 rounded-full bg-zinc-900 dark:bg-zinc-50" /> : 
                           <div className="h-2 w-2 rounded-full bg-zinc-200 dark:bg-zinc-800" />}
                        </div>
                        
                        <div className="md:text-center mt-1">
                          <p className={`text-sm font-semibold ${step.pending || step.completed ? 'text-zinc-900 dark:text-white' : 'text-zinc-500 dark:text-zinc-400'}`}>
                            {step.status}
                          </p>
                          <p className="text-xs text-zinc-500 mt-1">{step.date}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
