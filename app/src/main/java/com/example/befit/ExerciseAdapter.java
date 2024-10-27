package com.example.befit;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    private List<Exercise> exerciseList;

    // Default constructor
    public ExerciseAdapter() {
        exerciseList = new ArrayList<>();
    }

    // Constructor accepting List<Exercise>
    public ExerciseAdapter(List<Exercise> exerciseList) {
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_exercise, parent, false);
        return new ExerciseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exerciseList != null ? exerciseList.size() : 0;
    }

    public void setActivity(Activity activity) {
        // Use the activity instance when needed
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private ImageView exerciseImageView;
        private TextView exerciseNameTextView;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseImageView = itemView.findViewById(R.id.exerciseImageView);
            exerciseNameTextView = itemView.findViewById(R.id.exerciseNameTextView);
        }

        public void bind(Exercise exercise) {
            exerciseImageView.setImageResource(exercise.getImageResource());
            exerciseNameTextView.setText(exercise.getName());
        }
    }
}
