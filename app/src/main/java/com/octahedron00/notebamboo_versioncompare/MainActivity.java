package com.octahedron00.notebamboo_versioncompare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

	EditText editTextBefore, editTextAfter;
	TextView textCompare;
	Button buttonCopy, buttonCompare;
	private static int RED = 0xFFFF8888, GREEN = 0xFF88FF88;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		editTextBefore = findViewById(R.id.editTextBefore);
		editTextAfter = findViewById(R.id.editTextAfter);
		textCompare = findViewById(R.id.textCompare);
		buttonCopy = findViewById(R.id.buttonCopy);
		buttonCompare = findViewById(R.id.buttonCompare);

		buttonCopy.setOnClickListener(v -> {
			if(1000000<editTextAfter.getText().length()||1000000<editTextBefore.getText().length()){
				Toast.makeText(this, "text must be below 1000000", Toast.LENGTH_SHORT).show();
			}
			else if(2000<editTextAfter.getText().toString().split("\n").length||2000<editTextBefore.getText().toString().split("\n").length){
				Toast.makeText(this, "lines must be below 2000", Toast.LENGTH_SHORT).show();
			}

			editTextAfter.setText(editTextBefore.getText());
			editTextAfter.requestFocus();
			editTextAfter.setSelection(editTextAfter.getText().length());
		});

		buttonCompare.setOnClickListener(v -> textCompare.setText(merge(editTextAfter.getText().toString(),editTextBefore.getText().toString())));
	}

	public static Spannable merge(String after, String before){

		int afterLines, beforeLines, i, j, m, t;
		int[][] score = new int[2020][2020];
		int[][] pos = new int[2020][2020];
		CharSequence[] afterSequences = new CharSequence[2020];
		CharSequence[] beforeSequences = new CharSequence[2020];

		SpannableStringBuilder builder = new SpannableStringBuilder();

		SpannableStringBuilder afterBuilder = new SpannableStringBuilder(after).append("\n");
		SpannableStringBuilder beforeBuilder = new SpannableStringBuilder(before).append("\n");

		m = 0;
		t = 0;
		for(i=0; i<afterBuilder.length(); i++){
			if(afterBuilder.charAt(i)=='\n'){
				afterSequences[t] = afterBuilder.subSequence(m, i);
				t++;
//				Log.d("TAG", "merge: recent, "+m+"~"+i+"/"+t);
				m = i+1;
			}
		}
		afterLines = t;
		m = 0;
		t = 0;
		for(i=0; i<beforeBuilder.length(); i++){
			if(beforeBuilder.charAt(i)=='\n'){
				beforeSequences[t] = beforeBuilder.subSequence(m, i);
				t++;
//				Log.d("TAG", "merge: before, "+m+"~"+i+"/"+t);
				m = i+1;
			}
		}
		beforeLines = t;

		score[0][0] = 1;
		for(i=0; i<afterLines; i++){
			for(j=0; j<beforeLines; j++){
				int count = lineup(afterSequences[i], beforeSequences[j]);
				if(afterSequences[i].equals(beforeSequences[j])){
					if(score[i][j]>=score[i+1][j+1]){
						if(afterSequences[i].length()>2){
							score[i+1][j+1] = score[i][j]+afterSequences[i].length()*2;
						}
						else{
							score[i+1][j+1] = score[i][j]+2;
						}
						pos[i+1][j+1] = 3;
						Log.d("TAG", "merge: "+i+" "+j);
					}
				}
				if(score[i][j]>score[i][j+1]){
					score[i][j+1] = score[i][j];
					pos[i][j+1] = 2;
				}
				if(score[i][j]>score[i+1][j]){
					score[i+1][j] = score[i][j];
					pos[i+1][j] = 1;
				}
				if(score[i][j]+count>score[i+1][j+1]){
					score[i+1][j+1] = score[i][j] + count;
					pos[i+1][j+1] = 4;
				}
			}
		}

		i = afterLines;
		j = beforeLines;
//		Log.d("TAG", "merge: "+i+" "+j+" "+pos[i][j]);
		pos[i][j] = 3;
		while(i>0||j>0){
			if(pos[i][j]==1){
				i--;
				SpannableStringBuilder instant = new SpannableStringBuilder(afterSequences[i]);
				instant.setSpan(new BackgroundColorSpan(GREEN), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				builder.insert(0, "\n");
				builder.insert(0, instant);
//				Log.d("TAG", "merge: "+i+" "+j);
			}
			if(pos[i][j]==2){
				j--;
				SpannableStringBuilder instant = new SpannableStringBuilder(beforeSequences[j]);
				instant.setSpan(new BackgroundColorSpan(RED), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				builder.insert(0, "\n");
				builder.insert(0, instant);
//				Log.d("TAG", "merge: "+i+" "+j);
			}
			if(pos[i][j]==3){
				i--;
				j--;
				builder.insert(0, "\n");
				builder.insert(0, afterSequences[i]);
//				Log.d("TAG", "merge: "+i+" "+j);
			}
			if(pos[i][j]==4){
				i--;
				j--;
				SpannableStringBuilder instant = new SpannableStringBuilder(afterSequences[i]);
				instant.setSpan(new BackgroundColorSpan(0xff88ff88), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				builder.insert(0, "\n");
				builder.insert(0, instant);
				instant = new SpannableStringBuilder(beforeSequences[j]);
				instant.setSpan(new BackgroundColorSpan(0xffff8888), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				builder.insert(0, "\n");
				builder.insert(0, instant);
			}
		}
		return builder;
	}

	static int lineup(CharSequence a, CharSequence b){
		if(a.length()<1||b.length()<1) {
			return 0;
		}

		char[] as = a.toString().toCharArray();
		char[] bs = b.toString().toCharArray();

		int c = 0;

		for(int i=0; i<a.length()&&i<1000; i++){
			for(int j=0; j<b.length()&&j<1000; j++){
				if(as[i]==bs[j]){
					c++;
					break;
				}
			}
		}
		for(int j=0; j<b.length()&&j<1000; j++){
			for(int i=0; i<a.length()&&i<1000; i++){
				if(bs[j]==as[i]){
					c++;
					break;
				}
			}
		}
		if(c*2<as.length+bs.length) return 0;
		return c/2;
	}
}