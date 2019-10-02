package uk.co.vaguelycompetent.hackerman;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
public class AnswerDialog extends android.support.v4.app.DialogFragment {
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Boolean isCorrect = getArguments().getBoolean("correct");
        int difficulty = getArguments().getInt("difficulty");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(isCorrect) {
            builder.setTitle(R.string.correct);
            builder.setMessage(getResources().getQuantityString(R.plurals.space_to_move, difficulty, difficulty));
        }
        else
            builder.setTitle(R.string.wrong);


        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }
}
