package com.psu.capstonew17.backend.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.psu.capstonew17.backend.api.*;
import com.psu.capstonew17.backend.db.AslDbContract.*;
import com.psu.capstonew17.backend.db.AslDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExternalTestManager implements TestManager{
    public static ExternalTestManager INSTANCE = new ExternalTestManager();

    private AslDbHelper dbHelper;

    public static TestManager getInstance(Context context){
        INSTANCE.dbHelper = new AslDbHelper(context);
        return INSTANCE;
    }

    @Override
    public Test buildTest(List<Deck> sources, Options opts) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query;
        List<Question> questions = new ArrayList<Question>();
        for(Deck deck : sources) {
            query = "SELECT * FROM " + RelationEntry.TABLE_NAME +
                    " WHERE " + RelationEntry.COLUMN_DECK + "=" + ((ExternalDeck)deck).getDeckId();
            Cursor cursor = db.rawQuery(query, null);
            while(cursor.moveToNext()){
                int cardId = cursor.getInt(cursor.getColumnIndex(RelationEntry.COLUMN_CARD));
                Card card = ExternalCardManager.INSTANCE.getCard(cardId);
                Question.Type t;
                if(opts.questionTypes == opts.QUESTION_MULTIPLE_CHOICE){
                    t = Question.Type.MULTIPLE_CHOICE;
                }
                else{
                    t = Question.Type.TEXT_ENTRY;
                }
                Question q = new ExternalQuestion(card, t);
                questions.add(q);
            }
        }
        if(opts.mode.equals(OrderingMode.RANDOM)){
            Collections.shuffle(questions);
        }
        return new ExternalTest(questions.subList(0, opts.count));
    }
}
