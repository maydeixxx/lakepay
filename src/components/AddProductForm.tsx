import { useForm } from "react-hook-form";
import { Input } from "./Input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "./Select";
import { ADD_AD_URL, categories } from "@/config";
import { Textarea } from "./TextArea";
import { Button } from "./Button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from "./Form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAppSelector } from "@/redux/store";
import { useRef, useState } from "react";
import SpinnerIcon from "@/icons/SpinnerIcon";

const formSchema = z.object({
  title: z.string({ required_error: "Пожалуйста введите название." }),
  category: z.string({
    required_error: "Пожалуйста выберите категорию товара."
  }),
  description: z
    .string({ required_error: "Пожалуйста введите описание товара." })
    .max(150, "Описание не может быть длинее 150 символов."),
  details: z.string({
    required_error: "Пожалуйста введите детали от аккаунта."
  }),
  price: z.coerce
    .number({
      required_error: "Введите цену.",
      invalid_type_error: "Введите цену."
    })
    .min(1, "Цена не может быть меньше 1 руб."),
  quantity: z.coerce
    .number({
      required_error: "Введите кол-во товара.",
      invalid_type_error: "Введите кол-во товара."
    })
    .min(1, "Количество товара не может быть меньше 1.")
});

export function AddProductForm() {
  const { token, user } = useAppSelector((state) => state.auth);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [succeeded, setSucceeded] = useState(false);

  const abortControllerRef = useRef<AbortController | null>(null);

  async function onSubmit(values: z.infer<typeof formSchema>) {
    abortControllerRef.current?.abort();
    abortControllerRef.current = new AbortController();

    setIsLoading(true);
    try {
      const response = await fetch(ADD_AD_URL, {
        method: "post",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          title: values.title,
          body: values.description,
          category: values.category,
          price: values.price,
          quantity: values.quantity,
          sellerId: user?.id.toString(),
          sold: false
        })
      });

      if (response.ok) {
        setSucceeded(true);
      }
    } catch (e: any) {
      if (e.name === "AbortError") {
        console.log("Aborted");
        return;
      }

      setError(e);
    } finally {
      setIsLoading(false);
    }
  }

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema)
  });

  function resetForm() {
    form.reset();
    setIsLoading(false);
    setSucceeded(false);
    setError(null);
  }

  return (
    <>
      {isLoading && (
        <div className="flex w-full items-center justify-center py-16">
          <SpinnerIcon className="text-primary size-16" />
        </div>
      )}

      {succeeded && (
        <div className="flex w-full flex-col items-center py-16 gap-8">
          <h1 className="text-secondary text-4xl text-center">
            Объявление опубликовано!
          </h1>
          <Button variant="secondary" onClick={() => resetForm()}>
            Добавить объявление
          </Button>
        </div>
      )}

      {!isLoading && !succeeded && (
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
            <FormField
              control={form.control}
              name="title"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-secondary">Название:</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="Введите название..."
                      className="bg-on-card! placeholder:text-on-card-foreground"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="category"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-secondary">Категория:</FormLabel>
                  <FormControl>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                    >
                      <SelectTrigger className="w-full bg-secondary! data-[placeholder]:text-secondary-foreground text-secondary-foreground">
                        <SelectValue placeholder="Выберите категорию..." />
                      </SelectTrigger>
                      <SelectContent className="bg-secondary text-secondary-foreground">
                        {categories.map((category) => (
                          <SelectItem value={category.name} key={category.name}>
                            {category.title}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-secondary">Описание:</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="Введите описание..."
                      {...field}
                      className="resize-none h-32 bg-on-card! placeholder:text-on-card-foreground"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="details"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-secondary">Данные:</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="Введите данные..."
                      className="bg-on-card placeholder:text-on-card-foreground"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex gap-4 flex-col items-stretch md:items-start md:flex-row">
              <FormField
                control={form.control}
                name="price"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-secondary">Цена:</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        placeholder="Цена..."
                        className="bg-on-card placeholder:text-on-card-foreground"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="quantity"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-secondary">
                      Количество:
                    </FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        placeholder="Кол-во..."
                        className="bg-on-card placeholder:text-on-card-foreground"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Button variant="secondary" type="submit">
              Опубликовать объявление
            </Button>
          </form>
        </Form>
      )}
    </>
  );
}
