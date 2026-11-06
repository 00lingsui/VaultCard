import React, { useState } from 'react';
import { KOTLIN_CODEBASE, KotlinFile } from '../data/kotlinCodebase';
import { Copy, Check, FileCode, Layers, ShieldCheck, Download } from 'lucide-react';

export const CodeViewer: React.FC = () => {
  const [selectedFileId, setSelectedFileId] = useState<string>('entity');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const selectedFile = KOTLIN_CODEBASE.find((f) => f.id === selectedFileId) || KOTLIN_CODEBASE[0];

  const handleCopy = (code: string, id: string) => {
    navigator.clipboard.writeText(code);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleCopyAll = () => {
    const allCode = KOTLIN_CODEBASE.map(
      (f) => `// ==========================================\n// File: ${f.name}\n// Package: ${f.packagePath}\n// Module: ${f.module}\n// ==========================================\n\n${f.code}\n`
    ).join('\n\n');
    navigator.clipboard.writeText(allCode);
    setCopiedId('all');
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="space-y-6">
      {/* File Selector Bar */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-3">
          <div className="flex items-center gap-2">
            <Layers className="w-4 h-4 text-indigo-400" />
            <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-300">
              Kotlin Production Source Files
            </h2>
          </div>
          <button
            onClick={handleCopyAll}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-xs font-medium text-white transition-colors w-fit"
          >
            {copiedId === 'all' ? (
              <>
                <Check className="w-3.5 h-3.5" />
                Copied Full Codebase!
              </>
            ) : (
              <>
                <Copy className="w-3.5 h-3.5" />
                Copy All Kotlin Files (.kt)
              </>
            )}
          </button>
        </div>

        {/* File Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
          {KOTLIN_CODEBASE.map((file) => {
            const isSelected = file.id === selectedFileId;
            return (
              <button
                key={file.id}
                onClick={() => setSelectedFileId(file.id)}
                className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-mono transition-all whitespace-nowrap ${
                  isSelected
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                    : 'bg-slate-800/80 text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                }`}
              >
                <FileCode className="w-3.5 h-3.5" />
                <span>{file.name}</span>
                <span
                  className={`text-[10px] px-1.5 py-0.5 rounded ${
                    isSelected ? 'bg-indigo-800/80 text-indigo-200' : 'bg-slate-900 text-slate-400'
                  }`}
                >
                  {file.category}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Code Display Area */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl overflow-hidden shadow-2xl">
        {/* Code Header */}
        <div className="px-5 py-3.5 bg-slate-950 border-b border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-white font-mono text-sm font-semibold">{selectedFile.name}</span>
              <span className="text-[11px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 font-mono">
                {selectedFile.module}
              </span>
            </div>
            <div className="text-xs text-slate-400 font-mono mt-0.5">
              {selectedFile.packagePath}
            </div>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400 hidden md:inline">
              Kotlin 2.0+ / Coroutines Flow / Room 2.6
            </span>
            <button
              onClick={() => handleCopy(selectedFile.code, selectedFile.id)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-200 transition-colors"
            >
              {copiedId === selectedFile.id ? (
                <>
                  <Check className="w-3.5 h-3.5 text-emerald-400" />
                  <span className="text-emerald-400">Copied!</span>
                </>
              ) : (
                <>
                  <Copy className="w-3.5 h-3.5" />
                  <span>Copy File</span>
                </>
              )}
            </button>
          </div>
        </div>

        {/* File Description Banner */}
        <div className="px-5 py-2.5 bg-slate-900/50 border-b border-slate-800/60 text-xs text-slate-400 flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-indigo-400 shrink-0" />
          <span>{selectedFile.description}</span>
        </div>

        {/* Code Content with Syntax Look */}
        <div className="relative overflow-x-auto p-4 font-mono text-xs text-slate-200 leading-relaxed bg-[#0b0f19]">
          <pre className="overflow-x-auto">
            <code>
              {selectedFile.code.split('\n').map((line, idx) => (
                <div key={idx} className="table-row hover:bg-slate-800/30">
                  <span className="table-cell pr-4 text-right select-none text-slate-600 text-[11px] w-10">
                    {idx + 1}
                  </span>
                  <span className="table-cell whitespace-pre">{line}</span>
                </div>
              ))}
            </code>
          </pre>
        </div>
      </div>
    </div>
  );
};
