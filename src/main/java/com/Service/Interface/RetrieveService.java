package com.Service.Interface;

import com.Bean.FileDTO;
import com.Bean.ContentIdAndDate;

import java.util.List;

public interface RetrieveService {
    List<ContentIdAndDate> getFileCopies(Long fileName);

    FileDTO getSelectedFile(Long content_id);

    void deleteSelectedFile(Long content_id);
}
