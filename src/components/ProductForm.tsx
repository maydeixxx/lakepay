import { useForm, type SubmitHandler } from "react-hook-form";
import { Input } from "./Input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "./Select";
import { categories } from "@/config";

type Inputs = {
  title: string;
  category: string;

  exampleRequired: string;
};

export function ProductForm() {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors }
  } = useForm<Inputs>();
  const onSubmit: SubmitHandler<Inputs> = (data) => console.log(data);

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="flex flex-col gap-4 py-8"
    >
      <label htmlFor="title" className="text-secondary">
        Название:
      </label>
      <Input
        placeholder="Введите название..."
        {...register("title", { required: true })}
        className="bg-input"
      />
      <Select {...register("category", { required: true })}>
        <SelectTrigger className="border-primary w-full">
          <SelectValue placeholder="Выберите категорию..." />
        </SelectTrigger>
        <SelectContent>
          {categories.map((category) => (
            <SelectItem value={category.name} key={category.name}>
              {category.title}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Input {...register("exampleRequired", { required: true })} />
      {/* errors will return when field validation fails  */}
      {errors.exampleRequired && (
        <span className="text-destructive">Пожалуйста заполните все поля.</span>
      )}

      <Input type="submit" />
    </form>
  );
}
