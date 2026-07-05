import { Star } from 'lucide-react';

interface StarRatingProps {
  value: number;
  onChange?: (rating: number) => void;
  size?: number;
  readOnly?: boolean;
}

export function StarRating({ value, onChange, size = 26, readOnly = false }: StarRatingProps) {
  return (
    <div className="flex gap-1.5">
      {[1, 2, 3, 4, 5].map((star) => {
        const filled = star <= value;
        if (readOnly) {
          return (
            <Star
              key={star}
              size={size}
              className={filled ? 'fill-amber-400 text-amber-400' : 'text-brand-border'}
            />
          );
        }
        return (
          <button
            key={star}
            type="button"
            onClick={() => onChange?.(star)}
            aria-label={`${star}`}
            className="transition-transform hover:scale-110"
          >
            <Star
              size={size}
              className={filled ? 'fill-amber-400 text-amber-400' : 'text-brand-border'}
            />
          </button>
        );
      })}
    </div>
  );
}
