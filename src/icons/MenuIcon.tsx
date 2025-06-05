export function MenuIcon({ ...props }: React.ComponentProps<"svg">) {
  return (
    <svg
      width="24"
      height="24"
      viewBox="0 0 23 16"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <rect width="23" height="4" fill="currentColor" />
      <rect y="6" width="23" height="4" fill="currentColor" />
      <rect y="12" width="23" height="4" fill="currentColor" />
    </svg>
  );
}
