import { ProductView } from "@/components/ProductView";
import { useAppSelector } from "@/redux/store";

export default function CartPage() {
  const { items } = useAppSelector((state) => state.cart);

  return (
    <>
      <section className="container mx-auto py-16">
        <h1 className="text-secondary text-4xl mb-8">Ваша корзина:</h1>
        {Object.values(items).map((product) => (
          <ProductView product={product} />
        ))}
      </section>
    </>
  );
}
