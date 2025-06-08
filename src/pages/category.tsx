import { useParams } from "react-router";

export default function CategoryPage() {
  let { categoryId } = useParams<{ categoryId?: string }>();

  return (
    <>
      <section className="container mx-auto py-16">{categoryId}</section>
    </>
  );
}
