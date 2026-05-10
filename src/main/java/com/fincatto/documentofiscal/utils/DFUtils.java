package com.fincatto.documentofiscal.utils;

import java.util.Arrays;
import java.util.List;

public abstract class DFUtils {
    private static final List<String> CPFS_INVALIDOS = Arrays.asList("00000000000", "11111111111", "22222222222",
            "33333333333", "44444444444", "55555555555", "66666666666", "77777777777", "88888888888", "99999999999",
            "12345678909"
    );

    /**
     * Verifica se o CNPJ informado eh valido. <br>
     * Nao verifica o tamanho e presume que este seja de 14 digidos. Já suporta cnpj alpha
     *
     * @param cnpj CNPJ a ser validado.
     * @return Se o CNPJ informado eh valido ou nao.
     */
    public static boolean isCnpjValido(final String cnpj) {

        if (cnpj == null) {
            return false;
        }

        final String value = cnpj.toUpperCase();

        // 12 alphanumeric + 2 numeric
        if (!value.matches("^[A-Z0-9]{12}[0-9]{2}$")) {
            return false;
        }

        // false positives
        if (value.equals("00000000000000")) {
            return false;
        }

        final String base = value.substring(0, 12);

        final int dv1 = calcularDigito(base,
                new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});

        final int dv2 = calcularDigito(base + dv1,
                new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});

        return value.equals(base + dv1 + dv2);
    }

    private static int calcularDigito(final String value, final int[] pesos) {

        int soma = 0;

        for (int i = 0; i < value.length(); i++) {

            final char c = value.charAt(i);

            // ASCII - 48
            final int valor = ((int) c) - 48;

            soma += valor * pesos[i];
        }

        final int resto = soma % 11;

        return (resto < 2) ? 0 : 11 - resto;
    }

    /**
     * Verifica se o CPF informado eh valido. <br>
     * Nao verifica o tamanho e presume que este seja de 11 digidos e somente numeros.
     *
     * @param cpf CPF a ser validado.
     * @return Se o CPF informado eh valido ou nao.
     */
    public static boolean isCpfValido(final String cpf) {
        if (cpf == null || !cpf.matches("^[0-9]{11}$")) {
            return false;
        }

        // verifica por falsos positivos
        if (CPFS_INVALIDOS.contains(cpf)) {
            return false;
        }

        int d1 = 0, d2 = 0;
        for (int i = 1; i < 10; i++) {
            final int digitoCPF = Integer.parseInt(cpf.substring(i - 1, i));

            // multiplique a ultima casa por 2 a seguinte por 3 a seguinte por 4 e assim por diante
            d1 += (11 - i) * digitoCPF;

            // para o segundo digito repita o procedimento incluindo o primeiro digito calculado no passo anterior
            d2 += (12 - i) * digitoCPF;
        }

        // primeiro resto da divisao por 11
        int resto = d1 % 11;

        // se o resultado for 0 ou 1 o digito eh 0 caso contrario o digito eh 11 menos o resultado anterior
        final int digito1 = resto < 2 ? 0 : 11 - resto;

        // segundo resto da divisao por 11
        resto = (d2 + (2 * digito1)) % 11;
        // se o resultado for 0 ou 1 o digito eh 0 caso contrario o digito eh 11 menos o resultado anterior
        final int digito2 = resto < 2 ? 0 : 11 - resto;

        // digito verificador do CPF que estah sendo validado
        final String nDigVerific = cpf.substring(cpf.length() - 2);

        // concatenando o primeiro resto com o segundo
        final String nDigResult = String.valueOf(digito1) + digito2;

        // comparar o digito verificador do cpf com o primeiro resto + o segundo resto
        return nDigVerific.equals(nDigResult);
    }

    /**
     * Indica se a String informada é formada por somente caracteres numericos.
     * @param str String a ser verificada
     * @return Se a String é numerica.
     */
    public static boolean isNumerico(final String str) {
        return str != null && str.matches("\\d+");
    }
}
