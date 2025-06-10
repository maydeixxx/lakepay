import SpinnerIcon from "@/icons/SpinnerIcon";

export function Loading() {
  return (
    <>
      <section className="container mx-auto flex w-full justify-center h-96 items-center">
        <SpinnerIcon className="text-primary size-16" />
      </section>
    </>
  );
}
