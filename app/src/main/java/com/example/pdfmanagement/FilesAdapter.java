package com.example.pdfmanagement;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.VH> {
    private final Context context;
    private List<FileItem> data;

    public FilesAdapter(Context context, List<FileItem> data) {
        this.context = context;
        this.data = data;
    }

    public void updateList(List<FileItem> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final FileItem f = data.get(position);
        holder.title.setText(f.getName());
        holder.meta.setText(f.getMeta());

        // choose icon by file type - replace drawable names if yours are different
        int iconRes = R.drawable.baseline_pdf_file_24;
        String t = f.getType();
        if (t == null) t = "";
        switch (t) {
            case "doc": case "docx": iconRes = R.drawable.baseline_doc_file_24; break;
            case "xls": case "xlsx": iconRes = R.drawable.baseline_xml_file_24; break;
            case "ppt": case "pptx": iconRes = R.drawable.baseline_ppt_file_24; break;
            default: iconRes = R.drawable.baseline_pdf_file_24; break;
        }
        holder.icon.setImageResource(iconRes);

        holder.itemView.setOnClickListener(v -> {
            try {
                Uri contentUri = Uri.parse(f.getUri());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(contentUri, f.getMimeType());
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Can't open file", Toast.LENGTH_SHORT).show();
            }
        });

        holder.more.setOnClickListener(v -> {
            Toast.makeText(context, "More actions for: " + f.getName(), Toast.LENGTH_SHORT).show();
            // TODO: show popup menu for Delete/Share etc.
        });
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon, more;
        TextView title, meta;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            more = v.findViewById(R.id.more);
            title = v.findViewById(R.id.title);
            meta = v.findViewById(R.id.meta);
        }
    }
}
