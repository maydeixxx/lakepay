import { useForm } from "react-hook-form";
import { Input } from "./Input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "./Select";
import { ADD_AD_URL, categories, WITHDRAW_URL } from "@/config";
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
  amount: z.coerce
    .number({
      required_error: "Введите количество.",
      invalid_type_error: "Введите число."
    })
    .min(1, "Вывод не может быть меньше 1$.")
});

export function WithdrawForm() {
  const abortController = useRef<AbortController | null>(null);
  const { token, user } = useAppSelector((state) => state.auth);
  const [succeeded, setSucceeded] = useState(false);
  const [error, setError] = useState("");

  async function onSubmit(values: z.infer<typeof formSchema>) {
    abortController?.current?.abort();
    abortController.current = new AbortController();

    try {
      const response = await fetch(WITHDRAW_URL, {
        method: "post",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          userId: user?.id,
          currency: "TRX",
          amount: values.amount
        })
      });

      if (response.ok) {
        console.log(await response.json());
        setSucceeded(true);
      } else {
        setError(await response.text());
      }
    } catch (e: any) {
      if (e.name === "AbortError") {
        console.log("Aborted");
        return;
      }
      console.error(e);
    }
  }

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema)
  });

  function resetForm() {
    form.reset();
  }

  if (succeeded) {
    return (
      <div className="flex w-full flex-col items-center py-16 gap-8">
        <h1 className="text-secondary text-4xl text-center">
          Вывод прошел успешно!
        </h1>
      </div>
    );
  }

  return (
    <>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
          <FormField
            control={form.control}
            name="amount"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="text-secondary">Кол-во:</FormLabel>
                <FormControl>
                  <Input
                    placeholder="Количество..."
                    className="bg-on-card! placeholder:text-on-card-foreground"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {error && <p className="text-destructive text-sm">{error}</p>}
          <Button variant="secondary" type="submit">
            Вывести
          </Button>
        </form>
      </Form>
    </>
  );
}
