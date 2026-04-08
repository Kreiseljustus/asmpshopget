package io.github.kreiseljustus.asmputils.core.modules.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.*;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class EvalCommand implements ICommand{
    @Override
    public String getCommandName() {
        return "eval";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(
                argument("expr", StringArgumentType.greedyString()).executes(this::executeChecked)
        );
    }

    @Override
    public int execute(CommandContext<FabricClientCommandSource> context) {
        String expr = StringArgumentType.getString(context, "expr");

        double result = solve(expr);
        context.getSource().sendFeedback(Text.literal("Result: " + result));

        return 0;
    }

    private double solve(String expression) {
        return evalRPN(toRPN(expression));
    }

    //https://en.wikipedia.org/wiki/Reverse_Polish_notation
    private List<String> toRPN(String expr) {
        Map<String, Integer> priority = new HashMap<>();
        priority.put("+", 1);
        priority.put("-", 1);
        priority.put("*", 2);
        priority.put("/", 2);
        priority.put("^", 3);

        Map<String, Boolean> rightAssoc = new HashMap<>();
        rightAssoc.put("^", true);

        Stack<String> ops = new Stack<>();
        List<String> output = new ArrayList<>();

        int i = 0;
        while(i < expr.length()) {
            char c = expr.charAt(i);

            //Not needed currently but might be useful later
            if(Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if(Character.isDigit(c)) {
                StringBuilder num = new StringBuilder();
                while(i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i));
                    i++;
                }
                output.add(num.toString());
                continue;
            }

            if (c == '(') {
                ops.push("(");
            }
            else if (c == ')') {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
                ops.pop();
            } else {
                String op = String.valueOf(c);

                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    String top = ops.peek();

                    if ((!rightAssoc.getOrDefault(op, false) && priority.get(op) <= priority.get(top))
                            || (rightAssoc.getOrDefault(op, false) && priority.get(op) < priority.get(top)))
                    {
                        output.add(ops.pop());
                    }
                    else break;
                }

                ops.push(op);
            }

            i++;
        }

        while (!ops.isEmpty()) {
            output.add(ops.pop());
        }

        return output;
    }

    private double evalRPN(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            switch (token) {
                case "+":
                    stack.push(stack.pop() + stack.pop());
                    break;
                case "-": {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(a - b);
                    break;
                }
                case "*":
                    stack.push(stack.pop() * stack.pop());
                    break;
                case "/": {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(a / b);
                    break;
                }
                case "^": {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(Math.pow(a, b));
                    break;
                }
                default:
                    stack.push(Double.parseDouble(token));
                    break;
            }
        }

        return stack.pop();
    }
}
