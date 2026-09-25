package com.atguigo.springaiproject_1.funtion;

import okhttp3.Request;

import java.util.function.Function;

public class configfuntion implements Function<configfuntion.Request, configfuntion.Position> {
    @Override
    public Position apply(Request request){
        String position="";
        if (request.name.equals("张三")){
            position="算法工程师";
        }
        return new Position(position);
    }


    public record  Request(String name){}
    public record  Position(String Text){}
}
