export function TrashIcon({ ...props }: React.ComponentProps<"svg">) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      {...props}
    >
      <path
        fill="currentColor"
        d="M22 3v2H2V3h6V2h1V1h6v1h1v1zM4 7v15h1v1h14v-2h1V7zm12 12h-2V9h2zm-6 0H8V9h2z"
      />
    </svg>
  );
}
