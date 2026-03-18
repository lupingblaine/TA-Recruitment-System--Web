import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Plus, Users, Clock, Edit2, Trash2 } from 'lucide-react';
import { Link } from 'react-router';

export function OrganiserJobs() {
  const jobs = [
    {
      id: 1,
      module: 'CS201',
      title: 'Teaching Assistant - Data Structures',
      applicants: 45,
      shortlisted: 12,
      positions: 3,
      deadline: 'Oct 15, 2026',
      status: 'Active',
    },
    {
      id: 2,
      module: 'CS405',
      title: 'Lab Assistant - Machine Learning',
      applicants: 32,
      shortlisted: 8,
      positions: 2,
      deadline: 'Oct 20, 2026',
      status: 'Active',
    },
    {
      id: 3,
      module: 'CS101',
      title: 'Head TA - Intro to Programming',
      applicants: 89,
      shortlisted: 20,
      positions: 5,
      deadline: 'Oct 25, 2026',
      status: 'Draft',
    },
  ];

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">TA Positions</h1>
          <p className="text-zinc-500 mt-2">Manage your current job postings and create new ones.</p>
        </div>
        <Link to="/organiser/jobs/create">
          <Button className="gap-2 shrink-0">
            <Plus className="h-4 w-4" /> Create Posting
          </Button>
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-6">
        {jobs.map((job, i) => (
          <motion.div
            key={job.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.1 }}
          >
            <Card className="hover:shadow-md transition-shadow dark:hover:border-zinc-700">
              <CardContent className="p-6">
                <div className="flex flex-col md:flex-row gap-6 justify-between">
                  <div className="space-y-3">
                    <div className="flex items-center gap-3">
                      <Badge variant="neutral">{job.module}</Badge>
                      <Badge variant={job.status === 'Active' ? 'success' : 'outline'}>
                        {job.status}
                      </Badge>
                    </div>
                    <h3 className="text-xl font-bold text-zinc-900 dark:text-white">{job.title}</h3>
                    <div className="flex flex-wrap items-center gap-4 text-sm text-zinc-500">
                      <span className="flex items-center gap-1.5"><Clock className="h-4 w-4" /> Deadline: {job.deadline}</span>
                    </div>
                  </div>

                  <div className="flex flex-row md:flex-col justify-between items-end gap-4 md:gap-0">
                    <div className="flex items-center gap-6 text-center">
                      <div className="flex flex-col items-center">
                        <span className="text-2xl font-bold text-zinc-900 dark:text-white">{job.applicants}</span>
                        <span className="text-xs text-zinc-500 flex items-center gap-1"><Users className="h-3 w-3" /> Total</span>
                      </div>
                      <div className="h-8 w-px bg-zinc-200 dark:bg-zinc-800 hidden md:block" />
                      <div className="flex flex-col items-center">
                        <span className="text-2xl font-bold text-zinc-900 dark:text-white">{job.shortlisted}</span>
                        <span className="text-xs text-zinc-500">Shortlisted</span>
                      </div>
                      <div className="h-8 w-px bg-zinc-200 dark:bg-zinc-800 hidden md:block" />
                      <div className="flex flex-col items-center">
                        <span className="text-2xl font-bold text-zinc-900 dark:text-white">{job.positions}</span>
                        <span className="text-xs text-zinc-500">Positions</span>
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      <Button variant="outline" size="sm" className="gap-2">
                        <Edit2 className="h-3.5 w-3.5" /> Edit
                      </Button>
                      <Button variant="ghost" size="icon" className="text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/30">
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
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
