package ba.wurth.mb.Interfaces;

public class SpinnerItem {
    private Long _id;
    private String _name;
    private String _code;
    private String _value;

    public SpinnerItem(Long id, String name, String code, String value){
        this._id = id;
        this._name = name;
        this._code = code;
        this._value = value;
    }

    public void setId(Long id){
        this._id = id;
    }

    public Long getId(){
        return this._id;
    }

    public void setName(String name){
        this._name = name;
    }

    public String getName(){
        return this._name;
    }

    public void setCode(String code){
        this._code = code;
    }

    public String getCode(){
        return this._code;
    }

    public String getValue(){
        return this._value;
    }

}