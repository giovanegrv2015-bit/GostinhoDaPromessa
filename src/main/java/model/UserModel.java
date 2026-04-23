package model;


public class UserModel {
    
    private int id; 
    private String username;
    private String passwords;
    
    public UserModel(){
        
    }
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    
    public String getPassword() {
        return passwords;
    }
    public void setPassword(String passwords){
        this.passwords = passwords;
    }
}
