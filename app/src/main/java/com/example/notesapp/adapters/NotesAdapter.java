package com.example.notesapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.entities.Note;
import com.example.notesapp.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder>{

    private List<Note> noteList;
    private NotesListener noteListener;
    private int spanCount;
    private List<Note> noteSource;
    private Timer timer;

    public NotesAdapter(List<Note> noteList, NotesListener noteListener) {
        this.noteList = noteList;
        this.noteListener = noteListener;
        this.noteSource = noteList;
    }

    public void setSpanCount(int spanCount){
        this.spanCount = spanCount;
    }

    public int getSpanCount(){return this.spanCount;}

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(spanCount == 2){
            return new NoteViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_container_note_1, parent, false)
            );
        }else{
            return new NoteViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_container_note_2, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setNote(noteList.get(position), this.spanCount);
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteListener.onNoteClicked(noteList.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView textTitle, textSub, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        //ImageView imageNote;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSub = itemView.findViewById(R.id.textSub);
            textDateTime = itemView.findViewById(R.id.textDate);
            layoutNote = itemView.findViewById(R.id.layoutNotes);
            imageNote = itemView.findViewById(R.id.imageNoteView);
        }

        void setNote(Note note, int spanCount){
            textTitle.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty()){
                textSub.setVisibility(View.GONE);
            }else{
                if(spanCount == 2){
                    textSub.setText(note.getSubtitle());
                }
            }
            textDateTime.setText(note.getDateTime());

            GradientDrawable gd = (GradientDrawable) layoutNote.getBackground();
            if(note.getColor() != null){
                gd.setColor(Color.parseColor(note.getColor()));
            }else{
                gd.setColor(Color.parseColor("#333333"));
            }

            if(note.getImagePath() != null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }else{
                imageNote.setVisibility(View.GONE);
            }
        }
    }

    public void searchNote(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    noteList = noteSource;
                }else{
                    ArrayList<Note> temp = new ArrayList<>();
                    for(Note note : noteSource){
                        if(note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    noteList = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 300);
    }

    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }
}
