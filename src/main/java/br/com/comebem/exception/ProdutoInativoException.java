package br.com.comebem.exception;

public class ProdutoInativoException extends RuntimeException {
    public ProdutoInativoException(String message) {
        super(message);
    }
}
