import { useForm } from "react-hook-form";
import { Input } from "./Input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "./Select";
import { categories } from "@/config";
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

type Inputs = {
  title: string;
  category: string;
  description: string;
  details: string;
  errorInput: string;
};

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
  })
});

export function ProductForm() {
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema)
  });

  function onSubmit(values: z.infer<typeof formSchema>) {
    console.log(values);
  }

  return (
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
                <Select {...field}>
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
              <FormLabel className="text-secondary">Название:</FormLabel>
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

        <Button variant="secondary" type="submit">
          Опубликовать объявление
        </Button>
      </form>
    </Form>
  );
}
