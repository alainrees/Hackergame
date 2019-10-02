package uk.co.vaguelycompetent.hackerman;

import java.io.Serializable;

class Question implements Serializable{
    int difficulty;
    String selectorSubText;

    String questionTitle;
    int correctAnswer;
    String[] answers;

    // TODO : Validation
    Question(int difficulty, String selectorSubText, String questionTitle, int correctAnswer, String[] answers){
        this.difficulty = difficulty;
        this.selectorSubText = selectorSubText;
        this.questionTitle = questionTitle;
        this.correctAnswer = correctAnswer;
        this.answers = answers;
    }

    @Override
    public String toString(){
        return
                this.difficulty + ", " + this.selectorSubText + "\n" +
                this.questionTitle + "\n" +
                this.answers[0] + " || " + this.answers[1] + " || " + this.answers[2] + " || " + this.answers[3] + "\n" +
                this.correctAnswer;
    }
}
