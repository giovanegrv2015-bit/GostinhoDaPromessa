package model;

public class UserModel {

    private int id;
    private String username;
    private String passwords;
    private String funcao;

    public UserModel() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return passwords;
    }

    public void setPassword(String passwords) {
        this.passwords = passwords;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
        
}
}

