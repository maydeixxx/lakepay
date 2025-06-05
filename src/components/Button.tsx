import { cn } from "@/utils";

type ButtonVariant = "primary" | "secondary" | "destructive";
type ButtonSize = "sm" | "default" | "lg" | "icon";

const variantStyles: Record<ButtonVariant, string> = {
  primary: "bg-primary text-primary-foreground shadow-xs hover:bg-primary/90",
  secondary:
    "bg-secondary text-secondary-foreground shadow-xs hover:bg-secondary/80",
  destructive:
    "bg-destructive text-white shadow-xs hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:focus-visible:ring-destructive/40 dark:bg-destructive/60"
};

const sizeStyles: Record<ButtonSize, string> = {
  default: "h-8 px-4 py-6 has-[>img]:px-6",
  sm: "h-8 rounded-lg gap-1.5 px-3 has-[>img]:px-2.5",
  lg: "h-10 rounded-2lx px-6 has-[>img]:px-4",
  icon: "size-10"
};

interface ButtonProps extends React.ComponentProps<"button"> {
  variant?: ButtonVariant;
  size?: ButtonSize;
}

export function Button({
  variant = "primary",
  size = "default",
  children,
  className,
  ...props
}: ButtonProps) {
  const combinedClassname = cn(
    "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-xl text-sm font-medium transition-all disabled:pointer-events-none disabled:opacity-50 [>img]:pointer-events-none [>img:not([class*='size-'])]:size-4 shrink-0 [>img]:shrink-0 outline-none focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:ring-[3px] aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive",
    variantStyles[variant],
    sizeStyles[size],
    className
  );

  return (
    <button className={combinedClassname} {...props}>
      {children}
    </button>
  );
}
