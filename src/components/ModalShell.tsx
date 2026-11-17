import React, { useEffect } from 'react';
import { X } from 'lucide-react';

export interface ModalShellProps {
  isOpen: boolean;
  onClose: () => void;
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  icon?: React.ReactNode;
  headerRight?: React.ReactNode;
  footer?: React.ReactNode;
  maxWidth?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '4xl';
  borderClassName?: string;
  bodyClassName?: string;
  containerClassName?: string;
  closeLabel?: string;
  children: React.ReactNode;
}

const MAX_WIDTH_MAP: Record<NonNullable<ModalShellProps['maxWidth']>, string> = {
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-lg',
  xl: 'max-w-xl',
  '2xl': 'max-w-2xl',
  '4xl': 'max-w-4xl',
};

export const ModalShell: React.FC<ModalShellProps> = ({
  isOpen,
  onClose,
  title,
  subtitle,
  icon,
  headerRight,
  footer,
  maxWidth = 'lg',
  borderClassName = 'border-slate-800',
  bodyClassName = 'p-6 space-y-4 overflow-y-auto max-h-[75vh]',
  containerClassName = '',
  closeLabel = 'Close modal',
  children,
}) => {
  useEffect(() => {
    if (!isOpen) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4"
      role="dialog"
      aria-modal="true"
      onClick={e => {
        if (e.target === e.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        className={`relative w-full ${MAX_WIDTH_MAP[maxWidth]} bg-slate-900 border ${borderClassName} rounded-2xl overflow-hidden shadow-2xl flex flex-col ${containerClassName}`}
      >
        <div className="flex items-center justify-between gap-3 px-5 sm:px-6 py-3.5 sm:py-4 border-b border-slate-800 bg-slate-950/90">
          <div className="flex items-center gap-3 min-w-0">
            {icon && <div className="shrink-0">{icon}</div>}
            <div className="min-w-0">
              <h3 className="font-serif-title font-bold text-slate-100 text-base sm:text-lg truncate">
                {title}
              </h3>
              {subtitle && <p className="text-xs text-slate-400 truncate">{subtitle}</p>}
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            {headerRight}
            <button
              type="button"
              onClick={onClose}
              aria-label={closeLabel}
              className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        <div className={bodyClassName}>{children}</div>

        {footer}
      </div>
    </div>
  );
};
