import React from 'react';
import { motion } from 'motion/react';
import { Link } from 'react-router';
import { ChevronRight, Users, Briefcase, Zap, CheckCircle2 } from 'lucide-react';

export function Landing() {
  return (
    <div className="min-h-screen bg-white text-zinc-950 selection:bg-zinc-200 selection:text-zinc-900">
      <nav className="fixed inset-x-0 top-0 z-50 flex h-16 items-center justify-between px-6 border-b border-zinc-200/50 bg-white/70 backdrop-blur-md">
        <div className="flex items-center gap-2 font-semibold text-lg">
          <div className="h-8 w-8 rounded-lg bg-zinc-900 flex items-center justify-center shadow-sm">
            <span className="text-white text-sm font-bold">TA</span>
          </div>
          <span>RecruitPro</span>
        </div>
        <div className="flex items-center gap-4">
          <Link to="/auth/login" className="text-sm font-medium text-zinc-600 hover:text-zinc-900 transition-colors">Log in</Link>
          <Link to="/auth/signup" className="rounded-full bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-800 transition-colors shadow-sm">Get Started</Link>
        </div>
      </nav>

      <main className="pt-32 pb-16 px-6 sm:px-12 lg:px-24 max-w-7xl mx-auto flex flex-col items-center text-center">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="inline-flex items-center gap-2 rounded-full border border-zinc-200 bg-zinc-50 px-3 py-1 text-sm font-medium text-zinc-600 mb-8 shadow-sm"
        >
          <Zap className="h-4 w-4 text-amber-500" />
          <span>The new standard for TA recruitment</span>
        </motion.div>

        <motion.h1 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="text-5xl md:text-7xl font-bold tracking-tight text-zinc-950 max-w-4xl"
        >
          Streamline your teaching assistant hiring
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="mt-6 text-xl text-zinc-500 max-w-2xl"
        >
          A transparent, efficient, and beautiful platform for universities to manage TA applications, interviews, and offers in one place.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="mt-10 flex flex-col sm:flex-row items-center gap-4 w-full justify-center"
        >
          <Link to="/auth/signup" className="flex items-center justify-center gap-2 rounded-full bg-zinc-900 px-8 py-4 text-base font-semibold text-white hover:bg-zinc-800 transition-all hover:scale-105 active:scale-95 shadow-lg w-full sm:w-auto">
            Start hiring now
            <ChevronRight className="h-5 w-5" />
          </Link>
          <Link to="/auth/login" className="flex items-center justify-center gap-2 rounded-full border border-zinc-200 bg-white px-8 py-4 text-base font-semibold text-zinc-900 hover:bg-zinc-50 transition-all w-full sm:w-auto shadow-sm">
            View as Applicant
          </Link>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.7, delay: 0.4 }}
          className="mt-20 w-full max-w-5xl rounded-2xl border border-zinc-200/60 bg-white/50 p-2 shadow-2xl backdrop-blur-xl"
        >
          <div className="rounded-xl overflow-hidden border border-zinc-100 bg-zinc-50 aspect-video flex items-center justify-center text-zinc-400">
            <img 
              src="https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=2000&q=80" 
              alt="Dashboard Preview" 
              className="w-full h-full object-cover opacity-90 mix-blend-multiply filter grayscale-[30%] contrast-125"
            />
          </div>
        </motion.div>

        <div className="mt-32 grid grid-cols-1 md:grid-cols-3 gap-8 text-left w-full max-w-5xl">
          {[
            {
              icon: Users,
              title: "Unified Applicant Profiles",
              description: "Students create one profile and apply to multiple modules seamlessly."
            },
            {
              icon: Briefcase,
              title: "Transparent Tracking",
              description: "Real-time application status for both applicants and organisers."
            },
            {
              icon: CheckCircle2,
              title: "Automated Interviews",
              description: "Schedule and manage interview slots without the back-and-forth emails."
            }
          ].map((feature, i) => (
            <motion.div 
              key={i}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.1 }}
              className="p-6 rounded-2xl border border-zinc-200/50 bg-zinc-50/50 hover:bg-zinc-50 transition-colors shadow-sm"
            >
              <div className="h-12 w-12 rounded-xl bg-white border border-zinc-200 flex items-center justify-center mb-6 shadow-sm">
                <feature.icon className="h-6 w-6 text-zinc-900" />
              </div>
              <h3 className="text-lg font-semibold text-zinc-900 mb-2">{feature.title}</h3>
              <p className="text-zinc-500 leading-relaxed">{feature.description}</p>
            </motion.div>
          ))}
        </div>
      </main>
    </div>
  );
}
