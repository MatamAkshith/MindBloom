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
 * JournalAdapter custom ArrayAdapter to bind JournalEntry items into the ListView.
 * Follows the ListView and ArrayAdapter concepts demonstrated in Week 5(b) of the Lab Manual.
 */
public class JournalAdapter extends ArrayAdapter<JournalEntry> {

    public interface OnEntryActionListener {
        void onView(JournalEntry entry);
        void onEdit(JournalEntry entry);
        void onDelete(JournalEntry entry);
    }

    private final Context context;
    private final List<JournalEntry> entries;
    private final OnEntryActionListener listener;

    public JournalAdapter(@NonNull Context context, @NonNull List<JournalEntry> entries, OnEntryActionListener listener) {
        super(context, R.layout.item_journal_entry, entries);
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
            rowView = inflater.inflate(R.layout.item_journal_entry, parent, false);
        }

        JournalEntry entry = entries.get(position);

        View cardJournalItem = rowView.findViewById(R.id.cardJournalItem);
        TextView tvJournalTitle = rowView.findViewById(R.id.tvJournalTitle);
        TextView tvJournalPromptBadge = rowView.findViewById(R.id.tvJournalPromptBadge);
        TextView tvJournalTimestamp = rowView.findViewById(R.id.tvJournalTimestamp);
        TextView tvJournalSnippet = rowView.findViewById(R.id.tvJournalSnippet);
        Button btnRead = rowView.findViewById(R.id.btnReadEntry);
        Button btnEdit = rowView.findViewById(R.id.btnEditEntry);
        Button btnDelete = rowView.findViewById(R.id.btnDeleteEntry);

        if (entry != null) {
            // Display title or fallback to "Untitled Reflection"
            tvJournalTitle.setText(entry.getDisplayTitle());

            // Prompt badge display
            String promptText = entry.getPrompt();
            if (promptText != null && !promptText.trim().isEmpty()) {
                tvJournalPromptBadge.setText(promptText);
                tvJournalPromptBadge.setVisibility(View.VISIBLE);
            } else {
                tvJournalPromptBadge.setVisibility(View.GONE);
            }

            // Formatted date/time
            tvJournalTimestamp.setText(entry.getFormattedDate());

            // Content preview
            tvJournalSnippet.setText(entry.getContent());

            // Click listener on card and Read button opens full view
            View.OnClickListener viewListener = v -> {
                if (listener != null) {
                    listener.onView(entry);
                }
            };
            cardJournalItem.setOnClickListener(viewListener);
            btnRead.setOnClickListener(viewListener);

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
}
