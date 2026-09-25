package com.example.rpg.domain;
public record ExploreAction(String location) implements AdventureAction { public ExploreAction { location=Domain.cleanName(location,"location",60); } public String summary(){return "Explored "+location;} }
