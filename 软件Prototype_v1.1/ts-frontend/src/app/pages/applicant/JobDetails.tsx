import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { useParams, Link } from 'react-router';
import { Card, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { ChevronLeft, CheckCircle2, Clock, MapPin, Users, BookOpen, AlertCircle, Sparkles, TrendingUp, CalendarDays } from 'lucide-react';

export function JobDetails() {
  const { id } = useParams();
  const [isApplying, setIsApplying] = useState(false);
  const [step, setStep] = useState(1);
  const [submitted, setSubmitted] = useState(false);

  // Mock data
  const job = {
    title: 'Teaching Assistant - Data Structures',
    module: 'CS201',
    professor: 'Dr. Alan Turing',
    department: 'Computer Science',
    type: 'Part-time',
    hours: '10 hrs/week',
    hoursNum: 10,
    deadline: 'Oct 15, 2026',
    positions: 3,
    matchScore: 92,
    requirements: [
      'Minimum grade of A- in CS201 or equivalent',
      'Strong proficiency in Java or C++',
      'Excellent communication skills',
      'Available for 2 hours of lab sessions per week'
    ],
    responsibilities: [
      'Grade weekly programming assignments and provide detailed feedback',
      'Lead a 2-hour lab session for 30 students',
      'Hold 2 hours of office hours per week',
      'Assist with invigilating midterm and final exams'
    ]
  };

  const handleApply = () => setIsApplying(true);
  const nextStep = () => setStep(step + 1);
  const submitApplication = () => {
    setStep(3);
    setTimeout(() => {
      setSubmitted(true);
    }, 1500);
  };

  if (isApplying) {
    return (
      <div className="max-w-3xl mx-auto space-y-8">
        <Button variant="ghost" onClick={() => setIsApplying(false)} className="mb-4 text-zinc-500 hover:text-zinc-900">
          <ChevronLeft className="mr-2 h-4 w-4" /> Back to job details
        </Button>

        <Card className="overflow-hidden border-zinc-200/60 shadow-lg">
          <div className="bg-zinc-50 dark:bg-zinc-900 border-b border-zinc-200/60 dark:border-zinc-800/60 p-6 flex justify-between items-center">
            <div>
              <h2 className="text-xl font-bold text-zinc-900 dark:text-white">Apply for {job.module} TA</h2>
              <p className="text-zinc-500 mt-1 text-sm">Step {Math.min(step, 2)} of 2</p>
            </div>
            <div className="flex gap-2">
              <div className={`h-2 w-12 rounded-full ${step >= 1 ? 'bg-zinc-900 dark:bg-zinc-50' : 'bg-zinc-200 dark:bg-zinc-800'}`} />
              <div className={`h-2 w-12 rounded-full ${step >= 2 ? 'bg-zinc-900 dark:bg-zinc-50' : 'bg-zinc-200 dark:bg-zinc-800'}`} />
            </div>
          </div>

          <CardContent className="p-8">
            <AnimatePresence mode="wait">
              {step === 1 && (
                <motion.div
                  key="step1"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  className="space-y-6"
                >
                  <div className="bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-400 p-4 rounded-xl flex items-start gap-3 border border-blue-100 dark:border-blue-900/50">
                    <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
                    <p className="text-sm">We've pre-filled this application with your reusable profile. Please review the information below before proceeding.</p>
                  </div>

                  <div>
                    <h3 className="font-semibold text-zinc-900 dark:text-white mb-4">Academic Record</h3>
                    <div className="border border-zinc-200 dark:border-zinc-800 rounded-xl p-4 flex justify-between items-center bg-zinc-50/50 dark:bg-zinc-900/50">
                      <div>
                        <p className="font-medium text-zinc-900 dark:text-white">BSc Computer Science (Year 3)</p>
                        <p className="text-sm text-zinc-500">Current GPA: 3.8/4.0</p>
                      </div>
                      <Badge variant="success">Verified</Badge>
                    </div>
                  </div>

                  <div>
                    <h3 className="font-semibold text-zinc-900 dark:text-white mb-4">Relevant Coursework</h3>
                    <div className="border border-zinc-200 dark:border-zinc-800 rounded-xl divide-y divide-zinc-100 dark:divide-zinc-800">
                      <div className="p-4 flex justify-between items-center">
                        <span className="text-sm font-medium">CS201 Data Structures</span>
                        <Badge variant="neutral">Grade: A</Badge>
                      </div>
                      <div className="p-4 flex justify-between items-center">
                        <span className="text-sm font-medium">CS202 Algorithms</span>
                        <Badge variant="neutral">Grade: A-</Badge>
                      </div>
                    </div>
                  </div>

                  <div className="pt-6 border-t border-zinc-200 dark:border-zinc-800 flex justify-end">
                    <Button onClick={nextStep} size="lg">Continue to Documents</Button>
                  </div>
                </motion.div>
              )}

              {step === 2 && (
                <motion.div
                  key="step2"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  className="space-y-6"
                >
                  <div>
                    <h3 className="font-semibold text-zinc-900 dark:text-white mb-2">Supporting Documents</h3>
                    <p className="text-sm text-zinc-500 mb-6">Upload your CV and optional cover letter.</p>
                    
                    <div className="border-2 border-dashed border-zinc-200 dark:border-zinc-800 rounded-2xl p-8 flex flex-col items-center justify-center text-center bg-zinc-50/50 dark:bg-zinc-900/50 hover:bg-zinc-50 dark:hover:bg-zinc-900 transition-colors cursor-pointer">
                      <div className="h-12 w-12 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center text-zinc-500 mb-4">
                        <BookOpen className="h-6 w-6" />
                      </div>
                      <p className="font-medium text-zinc-900 dark:text-white">Click to upload or drag and drop</p>
                      <p className="text-sm text-zinc-500 mt-1">PDF up to 5MB</p>
                    </div>

                    <div className="mt-4 border border-zinc-200 dark:border-zinc-800 rounded-xl p-4 flex justify-between items-center bg-white dark:bg-zinc-950">
                      <div className="flex items-center gap-3">
                        <BookOpen className="h-5 w-5 text-zinc-400" />
                        <div>
                          <p className="text-sm font-medium text-zinc-900 dark:text-white">Alex_CV_2026.pdf</p>
                          <p className="text-xs text-zinc-500">2.4 MB • Uploaded from profile</p>
                        </div>
                      </div>
                      <Button variant="ghost" size="sm" className="text-red-500 hover:text-red-600">Remove</Button>
                    </div>
                  </div>

                  <div className="pt-6 border-t border-zinc-200 dark:border-zinc-800 flex justify-between items-center">
                    <Button variant="ghost" onClick={() => setStep(1)}>Back</Button>
                    <Button onClick={submitApplication} size="lg" className="gap-2">
                      Submit Application
                      <CheckCircle2 className="h-4 w-4" />
                    </Button>
                  </div>
                </motion.div>
              )}

              {step === 3 && (
                <motion.div
                  key="step3"
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  className="py-12 flex flex-col items-center justify-center text-center space-y-6"
                >
                  {submitted ? (
                    <>
                      <motion.div
                        initial={{ scale: 0 }}
                        animate={{ scale: 1 }}
                        transition={{ type: "spring", stiffness: 200, damping: 20 }}
                        className="h-20 w-20 rounded-full bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center text-emerald-600 dark:text-emerald-400"
                      >
                        <CheckCircle2 className="h-10 w-10" />
                      </motion.div>
                      <div>
                        <h2 className="text-2xl font-bold text-zinc-900 dark:text-white mb-2">Application Submitted!</h2>
                        <p className="text-zinc-500 max-w-md mx-auto">
                          Your application for {job.module} TA has been sent to Dr. Turing. You can track its status in your dashboard.
                        </p>
                      </div>
                      <Link to="/applicant/applications">
                        <Button size="lg" className="mt-4">Go to Applications Tracker</Button>
                      </Link>
                    </>
                  ) : (
                    <>
                      <div className="h-16 w-16 border-4 border-zinc-200 border-t-zinc-900 rounded-full animate-spin dark:border-zinc-800 dark:border-t-zinc-50" />
                      <p className="text-zinc-500 font-medium animate-pulse">Submitting your application...</p>
                    </>
                  )}
                </motion.div>
              )}
            </AnimatePresence>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <Link to="/applicant/jobs" className="inline-flex items-center text-sm font-medium text-zinc-500 hover:text-zinc-900 mb-2">
        <ChevronLeft className="mr-1 h-4 w-4" /> Back to jobs
      </Link>

      <div className="bg-white dark:bg-zinc-950 rounded-2xl border border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        <div className="p-8 sm:p-10 border-b border-zinc-200 dark:border-zinc-800 bg-gradient-to-br from-zinc-50 to-white dark:from-zinc-900/50 dark:to-zinc-950">
          <div className="flex flex-col sm:flex-row justify-between items-start gap-6">
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <Badge variant="neutral">{job.module}</Badge>
                <Badge variant={job.matchScore >= 90 ? 'success' : 'neutral'} className="gap-1 shadow-sm border border-emerald-200/50 dark:border-emerald-800/50 bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-300">
                  <Sparkles className="h-3 w-3" />
                  {job.matchScore}% Match
                </Badge>
              </div>
              <h1 className="text-3xl font-bold text-zinc-900 dark:text-white tracking-tight">{job.title}</h1>
              <div className="flex flex-wrap items-center gap-4 text-sm text-zinc-500">
                <span className="flex items-center gap-1.5"><Users className="h-4 w-4" /> {job.professor}</span>
                <span className="flex items-center gap-1.5"><MapPin className="h-4 w-4" /> {job.department}</span>
                <span className="flex items-center gap-1.5"><Clock className="h-4 w-4" /> {job.hours}</span>
              </div>
            </div>
            <div className="flex flex-col items-start sm:items-end gap-3 w-full sm:w-auto">
              <Button size="lg" onClick={handleApply} className="w-full sm:w-auto">Apply Now</Button>
              <p className="text-xs text-zinc-500 font-medium">Closes {job.deadline}</p>
            </div>
          </div>
        </div>

        <div className="p-8 sm:p-10 grid grid-cols-1 md:grid-cols-3 gap-10">
          <div className="md:col-span-2 space-y-8">
            <section>
              <div className="flex items-center gap-2 mb-4">
                <TrendingUp className="h-5 w-5 text-indigo-500" />
                <h3 className="text-lg font-bold text-zinc-900 dark:text-white">Workload & Schedule Impact</h3>
              </div>
              <Card className="bg-gradient-to-br from-indigo-50/50 to-purple-50/50 dark:from-indigo-950/20 dark:to-purple-950/20 border-indigo-100 dark:border-indigo-900/30 overflow-hidden shadow-sm">
                <CardContent className="p-6">
                  <div className="space-y-6">
                    <div>
                      <div className="flex justify-between text-sm mb-2 items-end">
                        <span className="text-indigo-900/80 dark:text-indigo-200/80 font-medium">Estimated Weekly Commitment</span>
                        <div className="text-right">
                          <div className="font-bold flex items-center gap-2 justify-end">
                            <span className="text-zinc-400 line-through font-medium">12 hrs</span>
                            <span className="text-indigo-900 dark:text-indigo-100 text-lg">22 hrs</span>
                          </div>
                          <span className="text-red-500 dark:text-red-400 text-xs font-medium flex items-center gap-1 mt-0.5">
                            <AlertCircle className="h-3 w-3" />
                            Slightly over capacity risk
                          </span>
                        </div>
                      </div>
                      <div className="h-3 w-full bg-indigo-200/50 dark:bg-indigo-900/50 rounded-full overflow-hidden flex shadow-inner">
                        <motion.div 
                          initial={{ width: 0 }}
                          animate={{ width: "60%" }}
                          transition={{ duration: 1, ease: "easeOut" }}
                          className="h-full bg-indigo-500"
                          title="Current Workload: 12 hrs"
                        />
                        <motion.div 
                          initial={{ width: 0 }}
                          animate={{ width: "40%" }}
                          transition={{ duration: 1, delay: 0.5, ease: "easeOut" }}
                          className="h-full bg-amber-400"
                          title="Proposed Addition: 8 hrs up to capacity"
                        />
                         <motion.div 
                          initial={{ width: 0 }}
                          animate={{ width: "10%" }}
                          transition={{ duration: 1, delay: 1, ease: "easeOut" }}
                          className="h-full bg-red-500 relative"
                          title="Overload: 2 hrs"
                        >
                          <div className="absolute inset-0 pattern-diagonal-lines pattern-zinc-900 pattern-bg-transparent pattern-size-1 pattern-opacity-20" />
                        </motion.div>
                      </div>
                      <div className="flex justify-between text-xs mt-2 text-indigo-800/60 dark:text-indigo-200/60 font-medium">
                        <span>Current: 12 hrs</span>
                        <span>Max Capacity: 20 hrs</span>
                      </div>
                    </div>
                    
                    <div className="border-t border-indigo-100/50 dark:border-indigo-900/30 pt-5">
                      <h4 className="text-sm font-semibold text-indigo-900 dark:text-indigo-100 mb-4 flex items-center gap-2">
                        <CalendarDays className="h-4 w-4 text-indigo-500" />
                        Proposed Weekly Schedule
                      </h4>
                      <div className="grid grid-cols-5 gap-3 text-center text-xs">
                        <div className="space-y-2">
                          <div className="font-semibold text-zinc-500 uppercase tracking-wider text-[10px]">Mon</div>
                          <div className="bg-indigo-100 dark:bg-indigo-900/40 text-indigo-700 dark:text-indigo-300 rounded-md p-2 shadow-sm border border-indigo-200/50 dark:border-indigo-800/50">
                            <div className="font-bold mb-0.5">CS201 Lab</div>
                            <div className="opacity-80">14:00-16:00</div>
                          </div>
                        </div>
                        <div className="space-y-2">
                          <div className="font-semibold text-zinc-500 uppercase tracking-wider text-[10px]">Tue</div>
                          <div className="bg-zinc-100 dark:bg-zinc-800/60 text-zinc-600 dark:text-zinc-400 rounded-md p-2 border border-zinc-200/50 dark:border-zinc-700/50">
                            <div className="font-bold mb-0.5">Classes</div>
                            <div className="opacity-80">10:00-12:00</div>
                          </div>
                        </div>
                        <div className="space-y-2">
                          <div className="font-semibold text-zinc-500 uppercase tracking-wider text-[10px]">Wed</div>
                          <div className="bg-indigo-100 dark:bg-indigo-900/40 text-indigo-700 dark:text-indigo-300 rounded-md p-2 shadow-sm border border-indigo-200/50 dark:border-indigo-800/50">
                            <div className="font-bold mb-0.5">Office Hrs</div>
                            <div className="opacity-80">15:00-17:00</div>
                          </div>
                        </div>
                        <div className="space-y-2">
                          <div className="font-semibold text-zinc-500 uppercase tracking-wider text-[10px]">Thu</div>
                          <div className="bg-zinc-100 dark:bg-zinc-800/60 text-zinc-600 dark:text-zinc-400 rounded-md p-2 border border-zinc-200/50 dark:border-zinc-700/50">
                            <div className="font-bold mb-0.5">Classes</div>
                            <div className="opacity-80">13:00-16:00</div>
                          </div>
                        </div>
                        <div className="space-y-2">
                          <div className="font-semibold text-zinc-500 uppercase tracking-wider text-[10px]">Fri</div>
                          <div className="bg-indigo-50 dark:bg-indigo-900/20 text-indigo-600/80 dark:text-indigo-400/80 rounded-md p-2 border border-dashed border-indigo-200 dark:border-indigo-800">
                            <div className="font-bold mb-0.5">Grading</div>
                            <div className="opacity-80">Flexible</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </section>

            <section>
              <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-4">Role Description</h3>
              <p className="text-zinc-600 dark:text-zinc-400 leading-relaxed">
                As a Teaching Assistant for CS201, you will play a crucial role in helping students understand fundamental data structures and algorithms. You will work closely with the professor and other TAs to ensure a supportive learning environment.
              </p>
            </section>

            <section>
              <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-4">Requirements</h3>
              <ul className="space-y-3">
                {job.requirements.map((req, i) => (
                  <li key={i} className="flex items-start gap-3 text-zinc-600 dark:text-zinc-400">
                    <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-500 mt-0.5" />
                    <span>{req}</span>
                  </li>
                ))}
              </ul>
            </section>

            <section>
              <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-4">Responsibilities</h3>
              <ul className="space-y-3">
                {job.responsibilities.map((res, i) => (
                  <li key={i} className="flex items-start gap-3 text-zinc-600 dark:text-zinc-400">
                    <div className="h-1.5 w-1.5 rounded-full bg-zinc-900 dark:bg-zinc-50 shrink-0 mt-2" />
                    <span>{res}</span>
                  </li>
                ))}
              </ul>
            </section>
          </div>

          <div className="space-y-6">
            <div className="bg-zinc-50 dark:bg-zinc-900/50 rounded-xl p-6 border border-zinc-200/60 dark:border-zinc-800/60">
              <h4 className="font-semibold text-zinc-900 dark:text-white mb-4">Job Overview</h4>
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-zinc-500 mb-1">Positions Available</p>
                  <p className="font-medium text-zinc-900 dark:text-white">{job.positions}</p>
                </div>
                <div>
                  <p className="text-sm text-zinc-500 mb-1">Employment Type</p>
                  <p className="font-medium text-zinc-900 dark:text-white">{job.type}</p>
                </div>
                <div>
                  <p className="text-sm text-zinc-500 mb-1">Department</p>
                  <p className="font-medium text-zinc-900 dark:text-white">{job.department}</p>
                </div>
              </div>
            </div>

            <div className="bg-blue-50/50 dark:bg-blue-900/10 rounded-xl p-6 border border-blue-100 dark:border-blue-900/30">
              <div className="flex items-start gap-3">
                <AlertCircle className="h-5 w-5 text-blue-500 shrink-0 mt-0.5" />
                <p className="text-sm text-blue-800 dark:text-blue-300">
                  Applications are reviewed on a rolling basis. Early application is encouraged.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
