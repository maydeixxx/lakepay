export function ChatIcon({ ...props }: React.ComponentProps<"svg">) {
  return (
    <svg
      width="37"
      height="37"
      viewBox="0 0 37 37"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <path
        d="M33.3 0H0V37H3.7V3.7H33.3V25.9H7.4V29.6H3.7V33.3H7.4V29.6H37V0H33.3Z"
        fill="currentColor"
      />
      <rect x="13" y="7" width="19" height="3" fill="currentColor" />
      <rect x="13" y="12" width="19" height="3" fill="currentColor" />
      <rect x="13" y="17" width="19" height="3" fill="currentColor" />
    </svg>
  );
}
