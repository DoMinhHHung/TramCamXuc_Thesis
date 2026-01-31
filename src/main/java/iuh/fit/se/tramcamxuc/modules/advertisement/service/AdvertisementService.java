package iuh.fit.se.tramcamxuc.modules.advertisement.service;

import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UploadAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdResponse;

public interface AdvertisementService {
    AdResponse uploadAdvertisement(UploadAdRequest request);
    AdResponse getRandomAdvertisement();
}