import axios from "axios";

export const baseURLL = "https://resumegenerator-2.onrender.com/";

export const axiosInstance = axios.create({
  baseURL: baseURLL,
});

export const generateResume = async (description) => {
  const response = await axiosInstance.post("/api/v1/resume", {
    userDescription: description,
  });

  return response.data;
};
