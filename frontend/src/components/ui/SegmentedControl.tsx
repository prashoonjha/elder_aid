interface SegmentedOption<T extends string> {
  value: T;
  label: string;
}

interface SegmentedControlProps<T extends string> {
  options: SegmentedOption<T>[];
  value: T;
  onChange: (value: T) => void;
}

export function SegmentedControl<T extends string>({ options, value, onChange }: SegmentedControlProps<T>) {
  return (
    <div className="mb-5 flex gap-2.5">
      {options.map((option) => {
        const isSelected = option.value === value;
        return (
          <button
            key={option.value}
            type="button"
            onClick={() => onChange(option.value)}
            className={`flex-1 rounded-card border px-4 py-3 text-center text-sm font-medium transition-colors ${
              isSelected
                ? 'border-2 border-brand-accent bg-brand-accentLight text-brand-accentDark'
                : 'border-brand-border text-brand-primary hover:bg-brand-surface'
            }`}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );
}
