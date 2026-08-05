import type { SVGProps } from "react";

/** Sutil ornamento inspirado em folhas de acanto — puramente decorativo. */
export function AcanthusCorner(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 64 64" fill="none" aria-hidden="true" {...props}>
      <path
        d="M2 2 C 16 6, 26 14, 30 26 M2 2 C 8 16, 16 24, 28 30 M12 4 C 18 10, 22 18, 22 26 M4 12 C 10 18, 18 22, 26 22"
        stroke="currentColor" strokeWidth="0.8" strokeLinecap="round" opacity="0.7"
      />
      <circle cx="30" cy="30" r="1.2" fill="currentColor" opacity="0.7" />
    </svg>
  );
}

export function OrnamentDivider({ className = "" }: { className?: string }) {
  return (
    <div className={`flex items-center gap-3 text-dourado ${className}`} aria-hidden="true">
      <span className="h-px flex-1 bg-gradient-to-r from-transparent to-current opacity-50" />
      <svg viewBox="0 0 40 12" className="h-3 w-10" fill="none">
        <path d="M0 6 C 8 0, 12 12, 20 6 C 28 0, 32 12, 40 6" stroke="currentColor" strokeWidth="0.8" />
        <circle cx="20" cy="6" r="1.2" fill="currentColor" />
      </svg>
      <span className="h-px flex-1 bg-gradient-to-l from-transparent to-current opacity-50" />
    </div>
  );
}

export function ScriptoWordmark({ className = "" }: { className?: string }) {
  return (
    <span className={`font-serif text-xl font-semibold tracking-tight ${className}`}>
      SC<span className="relative inline-block">R<span className="absolute inset-x-0 top-1/2 h-px bg-dourado" aria-hidden="true" /></span>I
      <span className="relative inline-block">P<span className="absolute inset-x-0 top-1/2 h-px bg-dourado" aria-hidden="true" /></span>TO
    </span>
  );
}
