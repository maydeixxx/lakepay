import { Button } from "@/components/Button";
import { NavLink, useParams } from "react-router";
import cartIcon from "@/assets/cart.svg";
import userIcon from "@/assets/user.svg";

export default function ProductPage() {
  let { productId } = useParams<{ productId?: string }>();

  return (
    <>
      <section className="container mx-auto py-16 flex flex-col">
        <h1 className="text-secondary text-4xl mb-12">Название товара</h1>
        <div className="flex gap-8 mb-12">
          <img
            src="https://placehold.co/600x400"
            alt=""
            className="rounded-xl"
          />
          <div className="flex flex-col gap-4">
            <h2 className="text-secondary text-3xl">Цена: $100</h2>
            <div className="flex gap-4 py-4">
              <Button variant="secondary">
                <img src={cartIcon} alt="" />
              </Button>
              <Button variant="secondary">Купить сейчас</Button>
            </div>
            <NavLink to={`/user/1`} className="flex items-center gap-4">
              <img src={userIcon} alt="" />
              <p className="text-secondary">SellerName</p>
            </NavLink>
            <p className="text-secondary">Продаж: 123</p>
          </div>
        </div>
        <h2 className="text-secondary text-3xl mb-12">Описание:</h2>
        <p className="text-secondary text-justify">
          Многие думают, что Lorem Ipsum - взятый с потолка псевдо-латинский
          набор слов, но это не совсем так. Его корни уходят в один фрагмент
          классической латыни 45 года н.э., то есть более двух тысячелетий
          назад. Ричард МакКлинток, профессор латыни из колледжа Hampden-Sydney,
          штат Вирджиния, взял одно из самых странных слов в Lorem Ipsum,
          "consectetur", и занялся его поисками в классической латинской
          литературе. В результате он нашёл неоспоримый первоисточник Lorem
          Ipsum в разделах 1.10.32 и 1.10.33 книги "de Finibus Bonorum et
          Malorum" ("О пределах добра и зла"), написанной Цицероном в 45 году
          н.э. Этот трактат по теории этики был очень популярен в эпоху
          Возрождения. Первая строка Lorem Ipsum, "Lorem ipsum dolor sit
          amet..", происходит от одной из строк в разделе 1.10.32 Классический
          текст Lorem Ipsum, используемый с XVI века, приведён ниже. Также даны
          разделы 1.10.32 и 1.10.33 "de Finibus Bonorum et Malorum" Цицерона и
          их английский перевод, сделанный H. Rackham, 1914 год.
        </p>
        {productId}
      </section>
    </>
  );
}
