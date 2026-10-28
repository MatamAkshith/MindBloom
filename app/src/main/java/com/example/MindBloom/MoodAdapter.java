package com.example.MindBloom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * MoodAdapter custom ArrayAdapter to bind MoodEntry items into the ListView.
 * Follows the ListView and ArrayAdapter concepts demonstrated in Week 5(b) of the Lab Manual.
 */
public class MoodAdapter extends ArrayAdapter<MoodEntry> {

    public interface OnEntryActionListener {
        void onEdit(MoodEntry entry);
        void onDelete(MoodEntry entry);
    }

    private final Context context;
    private final List<MoodEntry> entries;
    private final OnEntryActionListener listener;

    public MoodAdapter(@NonNull Context context, @NonNull List<MoodEntry> entries, OnEntryActionListener listener) {
        super(context, R.layout.item_mood_entry, entries);
        this.context = context;
        this.entries = entries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.item_mood_entry, parent, false);
        }

        MoodEntry entry = entries.get(position);

        TextView tvMoodBadge = rowView.findViewById(R.id.tvMoodBadge);
        TextView tvIntensity = rowView.findViewById(R.id.tvIntensity);
        TextView tvTimestamp = rowView.findViewById(R.id.tvTimestamp);
        TextView tvNote = rowView.findViewById(R.id.tvNote);
        Button btnEdit = rowView.findViewById(R.id.btnEditEntry);
        Button btnDelete = rowView.findViewById(R.id.btnDeleteEntry);

        if (entry != null) {
            // Mood badge display (with friendly emoji prefix)
            tvMoodBadge.setText(formatMoodWithIcon(entry.getMood()));

            // Intensity label (1 to 5)
            String intensityText = "Intensity: " + entry.getIntensity() + "/5";
            tvIntensity.setText(intensityText);

            // Formatted date/time
            tvTimestamp.setText(entry.getFormattedDate());

            // Reflection note
            if (entry.getNote() != null && !entry.getNote().trim().isEmpty()) {
                tvNote.setText(entry.getNote());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }

            // Action listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(entry);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(entry);
                }
            });
        }

        return rowView;
    }

    private String formatMoodWithIcon(String mood) {
        if (mood == null) return "Mood";
        switch (mood) {
            case "Happy":
                return "🌸 Happy";
            case "Calm":
                return "🌿 Calm";
            case "Okay":
                return "🌱 Okay";
            case "Sad":
                return "🌧️ Sad";
            case "Stressed":
                return "⚡ Stressed";
            default:
                return mood;
        }
    }
}
