import React from 'react';
import { motion } from 'motion/react';
import { Card, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { User, Book, Clock, Edit2, UploadCloud, GraduationCap, Code } from 'lucide-react';

export function ApplicantProfile() {
  return (
    <div className="space-y-8 max-w-4xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white">Profile</h1>
          <p className="text-zinc-500 mt-2">Manage your reusable applicant profile.</p>
        </div>
        <div className="text-right">
          <span className="text-sm font-medium text-emerald-600 dark:text-emerald-400">85% Complete</span>
          <div className="mt-2 h-2 w-32 bg-zinc-100 dark:bg-zinc-800 rounded-full overflow-hidden">
            <div className="h-full bg-emerald-500 dark:bg-emerald-400 w-[85%]" />
          </div>
        </div>
      </div>

      <div className="grid gap-8">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
          <Card>
            <div className="p-6 border-b border-zinc-100 dark:border-zinc-800 flex justify-between items-center">
              <h2 className="text-xl font-semibold flex items-center gap-2 text-zinc-900 dark:text-white">
                <User className="h-5 w-5 text-zinc-500" /> Personal Information
              </h2>
              <Button variant="ghost" size="sm" className="gap-2">
                <Edit2 className="h-4 w-4" /> Edit
              </Button>
            </div>
            <CardContent className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-medium text-zinc-500">Full Name</label>
                <p className="font-medium text-zinc-900 dark:text-white">Alex Johnson</p>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-zinc-500">Student ID</label>
                <p className="font-medium text-zinc-900 dark:text-white">S12345678</p>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-zinc-500">Email Address</label>
                <p className="font-medium text-zinc-900 dark:text-white">alex.j@university.edu</p>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-zinc-500">Phone Number</label>
                <p className="font-medium text-zinc-900 dark:text-white">+1 (555) 123-4567</p>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
          <Card>
            <div className="p-6 border-b border-zinc-100 dark:border-zinc-800 flex justify-between items-center">
              <h2 className="text-xl font-semibold flex items-center gap-2 text-zinc-900 dark:text-white">
                <GraduationCap className="h-5 w-5 text-zinc-500" /> Academic Details
              </h2>
              <Button variant="ghost" size="sm" className="gap-2">
                <Edit2 className="h-4 w-4" /> Edit
              </Button>
            </div>
            <CardContent className="p-6 space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-zinc-500">Degree Program</label>
                  <p className="font-medium text-zinc-900 dark:text-white">BSc Computer Science</p>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-zinc-500">Current Year</label>
                  <p className="font-medium text-zinc-900 dark:text-white">Year 3</p>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-zinc-500">Cumulative GPA</label>
                  <p className="font-medium text-zinc-900 dark:text-white">3.8 / 4.0</p>
                </div>
              </div>

              <div>
                <label className="text-sm font-medium text-zinc-500 mb-2 block">Relevant Coursework</label>
                <div className="flex flex-wrap gap-2">
                  {['CS201 Data Structures (A)', 'CS202 Algorithms (A-)', 'CS301 Databases (B+)'].map(course => (
                    <span key={course} className="px-3 py-1 bg-zinc-100 dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300 rounded-full text-sm border border-zinc-200 dark:border-zinc-700">
                      {course}
                    </span>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
          <Card>
            <div className="p-6 border-b border-zinc-100 dark:border-zinc-800 flex justify-between items-center">
              <h2 className="text-xl font-semibold flex items-center gap-2 text-zinc-900 dark:text-white">
                <Book className="h-5 w-5 text-zinc-500" /> Documents
              </h2>
            </div>
            <CardContent className="p-6 space-y-6">
              <div className="border border-zinc-200 dark:border-zinc-800 rounded-xl p-4 flex justify-between items-center bg-zinc-50 dark:bg-zinc-900/50">
                <div className="flex items-center gap-4">
                  <div className="h-10 w-10 bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded-lg flex items-center justify-center">
                    <FileIcon className="h-5 w-5" />
                  </div>
                  <div>
                    <p className="font-medium text-zinc-900 dark:text-white">Alex_CV_2026.pdf</p>
                    <p className="text-sm text-zinc-500">Uploaded 2 days ago • 2.4 MB</p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="ghost" size="sm">Update</Button>
                  <Button variant="ghost" size="sm" className="text-red-500 hover:text-red-600">Delete</Button>
                </div>
              </div>

              <div className="border-2 border-dashed border-zinc-200 dark:border-zinc-800 rounded-xl p-6 flex flex-col items-center justify-center text-center cursor-pointer hover:bg-zinc-50 dark:hover:bg-zinc-900 transition-colors">
                <UploadCloud className="h-8 w-8 text-zinc-400 mb-2" />
                <p className="font-medium text-zinc-900 dark:text-white">Upload Transcript</p>
                <p className="text-sm text-zinc-500">PDF up to 5MB</p>
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}

function FileIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
      <polyline points="14 2 14 8 20 8" />
    </svg>
  );
}
